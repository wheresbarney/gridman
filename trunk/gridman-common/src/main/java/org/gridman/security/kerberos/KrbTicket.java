package org.gridman.security.kerberos;

import org.gridman.security.JaasHelper;
import org.gridman.security.kerberos.activedirectory.Pac;
import sun.security.krb5.EncryptionKey;
import sun.security.krb5.internal.AuthorizationData;
import sun.security.krb5.internal.EncTicketPart;
import sun.security.krb5.internal.Ticket;
import sun.security.krb5.internal.crypto.KeyUsage;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import javax.security.auth.Subject;
import javax.security.auth.kerberos.KerberosKey;
import java.io.IOException;
import java.security.Principal;
import java.util.Set;

/**
 * This class represents a decrypted Kerberos ticket.
 * </p>
 * Instances of the class are created by calling the static newInstance() method passing
 * in the encrypted ticket data and the Subject to use to decrypt the ticket.
 * </p>
 *
 * @author Jonathan Knight
 */
public class KrbTicket implements Principal {

    private byte[] serviceTicket;
    private EncryptionKey sessionKey;
    private String clientPrincipalName;
    private AuthorizationData authorizationData;

    private Pac pac;

    /**
     * Create a Kerberos Ticket. This takes the service ticket that is
     * to be decoded and the JAAS subject that contains the secret key for the
     * target service.
     *
     * @param serviceTicket the AP-REQ service ticket that is to be decode
     * @param subject       the JAAS subject containing the secret key for the server
     *                      principal
     * @return a new instance of a KrbTicket wrapping the given service ticket.
     * @throws SecurityException if there is an error decrypting the ticket.
     */
    public static KrbTicket newInstance(byte[] serviceTicket, Subject subject) {
        KrbTicket ticket = new KrbTicket(serviceTicket);
        ticket.parseServiceTicket(subject);
        return ticket;
    }

    /**
     * Construct a Kerberos Ticket Decoder. This takes the service ticket that is
     * to be decoded and the JAAS subject that contains the secret key for the
     * target service.
     *
     * @param serviceTicket the AP-REQ service ticket that is to be decode
     */
    KrbTicket(byte[] serviceTicket) {
        this.serviceTicket = serviceTicket;
    }

    public byte[] getAuthorizationData() {
        try {
            return authorizationData.asn1Encode();
        } catch (Exception e) {
            throw JaasHelper.ensureSecurityException(e);
        }
    }

    /**
     * Returns the Active Directory PACOld data extracted from the ticket.
     * </p>
     * @return the Active Directory PACOld data extracted from the ticket.
     */
    public byte[] getPacData() {
        try {
            byte[] pacData = null;
            byte[] kerberosRelevantAuthData = parseAuthData(getAuthorizationData(), 1);
            if (kerberosRelevantAuthData != null) {
                pacData = parseAuthData(kerberosRelevantAuthData, 128);
            }
            return pacData;
        } catch (IOException e) {
            throw JaasHelper.ensureSecurityException(e);
        }
    }

    public Pac getPAC() {
        return pac;
    }

    /**
     * Parses a DER encoded byte stream to extract the required data.
     *
     * @param token - the DER encoded stream to parse
     * @param requiredType - the required authentication data to extract
     * @return the extracted octets from the required authentication data
     * @throws java.io.IOException if an IO error occurs
     */
    public byte[] parseAuthData(byte[] token, int requiredType) throws IOException {
        byte[] octets = null;

        DerInputStream dis = new DerInputStream(token);
        DerValue derValue = dis.getDerValue();
        DerValue[] seqValues = derValue.getData().getSequence(2);
        int authType = seqValues[0].getData().getInteger();
        if (authType == requiredType) {
            octets = seqValues[1].getData().getOctetString();
        }

        return octets;
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    @Override
    public String getName() {
        return getClientPrincipalName();
    }

    /**
     * Get the client principal name from the decoded service ticket.
     *
     * @return the client principal name
     */
    public String getClientPrincipalName() {
        return clientPrincipalName;
    }

    /**
     * Get the session key from the decoded service ticket.
     *
     * @return the session key
     */
    EncryptionKey getSessionKey() {
        return sessionKey;
    }

    /**
     * Parse this instances service ticket using the specified {@link javax.security.auth.Subject}
     * to decrypt the ticket data.
     * </p>
     *
     * @param subject - the Subject to use to decrypt the ticket
     * @throws SecurityException - if there is an error
     */
    private void parseServiceTicket(Subject subject) {
        try {
            DerInputStream ticketStream = new DerInputStream(serviceTicket);
            DerValue[] values = ticketStream.getSet(serviceTicket.length, true);

            // Look for the AP_REQ.
            //
            //  AP-REQ ::= [APPLICATION 14] SEQUENCE
            for (DerValue value : values) {
                if (value.isConstructed((byte) 14)) {
                    value.resetTag(DerValue.tag_Set);
                    parseApReq(subject, value.toDerInputStream(), value.length());
                    return;
                }
            }
        } catch (Exception e) {
            throw new SecurityException("Error parsing service ticket", e);
        }

        byte[] pacData = getPacData();
        if (pacData != null) {
            pac = new Pac(pacData);
        }
        throw new SecurityException("Could not find AP-REQ in service ticket.");
    }

    // Parse the GSS AP-REQ token.
    private void parseApReq(Subject subject, DerInputStream reqStream, int len) throws Exception {
        DerValue ticket = null;

        DerValue[] values = reqStream.getSet(len, true);

        //
        //  AP-REQ ::= {
        //         pvno[0]                       INTEGER,
        //         msg-type[1]                   INTEGER,
        //         ap-options[2]                 APOptions,
        //         ticket[3]                     Ticket,
        //         authenticator[4]              EncryptedData
        //       }
        //
        for (DerValue value : values) {
            if (value.isContextSpecific((byte) 2)) {
                //apOptions = value.getData().getDerValue().getBitString()[0];
                // apOptions not used yet.
            } else if (value.isContextSpecific((byte) 3)) {
                ticket = value.getData().getDerValue();
            }
        }

        if (ticket == null) {
            throw new SecurityException("No Ticket found in AP-REQ PDU");
        }
        decryptTicket(new Ticket(ticket), subject);
    }

    // Decrypt the ticket.
    // APOptions ::=   BIT STRING {
    //                 reserved(0),
    //                 use-session-key(1),
    //                 mutual-required(2)
    // }
    //  Ticket ::=                    [APPLICATION 1] SEQUENCE {
    //                tkt-vno[0]                   INTEGER,
    //                realm[1]                     Realm,
    //                sname[2]                     PrincipalName,
    //                enc-part[3]                  EncryptedData
    //  }
    //
    //  EncTicketPart ::=     [APPLICATION 3] SEQUENCE {
    //        flags[0]             TicketFlags,
    //        key[1]               EncryptionKey,
    //        crealm[2]            Realm,
    //        cname[3]             PrincipalName,
    //        transited[4]         TransitedEncoding,
    //        authtime[5]          KerberosTime,
    //        starttime[6]         KerberosTime OPTIONAL,
    //        endtime[7]           KerberosTime,
    //        renew-till[8]        KerberosTime OPTIONAL,
    //        caddr[9]             HostAddresses OPTIONAL,
    //        authorization-data[10]   AuthorizationData OPTIONAL - using Active Directory
    //                                 this will be where the Groups are stored
    //  }

    private void decryptTicket(Ticket ticket, Subject svrSub) {
        try {
            // Get the private key that matches the encryption type of the ticket.
            EncryptionKey key = getPrivateKey(svrSub, ticket.encPart.getEType());

            // Decrypt the service ticket and get the cleartext bytes.
            byte[] ticketBytes = ticket.encPart.decrypt(key, KeyUsage.KU_TICKET);
            if (ticketBytes.length <= 0) {
                throw new SecurityException("Decrypted Key is empty.");
            }

            // EncTicketPart provides access to the decrypted attributes of the service
            // ticket.
            byte[] temp = ticket.encPart.reset(ticketBytes, true);
            EncTicketPart encPart = new EncTicketPart(temp);
            this.sessionKey = encPart.key;
            this.clientPrincipalName = encPart.cname.toString();
            this.authorizationData = encPart.authorizationData;
            
        } catch (Exception e) {
            throw JaasHelper.ensureSecurityException(e, "Error decrypting token");
        }
    }

    /**
     * Obtain the private server key of the specified type from
     * the given {@link javax.security.auth.Subject}.
     * </p>
     * The key type can be specified using constants in the
     * {@link sun.security.krb5.internal.crypto.KeyUsage} class.
     *
     * @param subject - the {@link javax.security.auth.Subject} to obtain the private key from
     * @param keyType - the type of the private key to obtain
     * @return the required private key
     */
    EncryptionKey getPrivateKey(Subject subject, int keyType) {
        KerberosKey key = getKrbKey(subject, keyType);
        return new EncryptionKey(key.getEncoded(), key.getKeyType(), keyType);
    }

    /**
     * Obtain the Kerberos key of the specified type from
     * the given {@link javax.security.auth.Subject}.
     * </p>
     * The key type can be specified using constants in the
     * {@link sun.security.krb5.internal.crypto.KeyUsage} class.
     *
     * @param subject - the {@link javax.security.auth.Subject} to obtain the Kerberos key from
     * @param keyType - the type of the private key to obtain
     * @return the required private key
     */
    private KerberosKey getKrbKey(Subject subject, int keyType) {
        Set<Object> credentials = subject.getPrivateCredentials(Object.class);
        for (Object cred : credentials) {
            if (cred instanceof KerberosKey) {
                KerberosKey key = (KerberosKey) cred;
                if (key.getKeyType() == keyType) {
                    return (KerberosKey) cred;
                }
            }
        }
        return null;
    }
}
