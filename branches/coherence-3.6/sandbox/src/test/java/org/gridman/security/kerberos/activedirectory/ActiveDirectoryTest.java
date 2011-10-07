package org.gridman.security.kerberos.activedirectory;

import com.sun.security.auth.module.Krb5LoginModule;
import com.tangosol.io.Base64OutputStream;
import org.apache.directory.server.core.integ.Level;
import org.apache.directory.server.core.integ.annotations.CleanupLevel;
import org.gridman.security.kerberos.KrbHelper;
import org.gridman.security.kerberos.KrbTicket;
import org.gridman.security.kerberos.junit.JAAS;
import org.gridman.security.kerberos.junit.KdcType;
import org.gridman.security.kerberos.junit.KiRunner;
import org.gridman.security.kerberos.junit.Krb5;
import org.gridman.security.kerberos.junit.activedirectory.ActiveDirectoryServerContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.security.util.DerInputStream;
import sun.security.util.DerValue;

import javax.security.auth.Subject;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.ByteBuffer;

/**
 * @author Jonathan Knight
 */
@RunWith(KiRunner.class)
@KdcType(type = ActiveDirectoryServerContext.class)
@CleanupLevel(Level.CLASS)
@Krb5( credentials = {
        "knightj@KNIGHT.COM,Secret1",
        "coherenceserver@KNIGHT.COM,Secret1"
       }
     , keytabFile = "jk-test.keytab"
     , krb5Conf = "jk-krb5.conf"
     , host = "192.168.162.172"
     , realm = "KNIGHT.COM"
)
@JAAS( fileName = "jk-test-jaas.conf"
     , moduleName = "com.sun.security.jgss.accept"
     , loginModuleClass = Krb5LoginModule.class
     , settings = {
        "required",
        "useKeyTab=true",
        "principal=\"knightj@KNIGHT.COM\"",
        "keyTab=\"./jk-test.keytab\"",
        "debug=true",
        "storeKey=true",
        "realm=\"KNIGHT.COM\"",
        "doNotPrompt=true",
        "useTicketCache=true",
        "renewTGT=true"
     }
)
/*
    required
    useKeyTab=true
    principal="coherenceserver@KNIGHT.COM"
    keyTab="./cs.keytab"
    debug=true
    storeKey=true
    realm="KNIGHT.COM"
    doNotPrompt=true
    useTicketCache=true
	renewTGT=true
	serviceprincipalname="coherenceserver@KNIGHT.COM";

	Coherence group SID
	hex: 01 05 00 00 00 00 00 05 15 00 00 00 18 04 BB 68 3B 92 2C 10 D9 1E CB C1 52 04 00 00

    30 82 02 FF 30 82 02 FB - A0 03 02 01 01 A1 82 02  Reserved0
    F2 04 82 02 EE 30 82 02 - EA 30 82 02 E6 A0 04 02  Reserved1
    02 00 80 A1 82 02 DC 04 - 82 02 D8 05 00 00 00 00  KickOffTime
    00 00 00 01 00 00 00 C8 - 01 00 00 58 00 00 00 00  Reserved2
    00 00 00 0A 00 00 00 28 - 00 00 00 20 02 00 00 00  Reserved3
    00 00 00 0C 00 00 00 60 - 00 00 00 48 02 00 00 00  Reserved4
    00 00 00 06 00 00 00 14 - 00 00 00 A8 02 00 00 00 
    00 00 00 07 00 00 00 14 00 00 00 C0 02 00 00 00 00 00 00 01 10 08 00 CC CC CC CC B8 01 00 00 00 00 00 00 00 00 02 00 5C DA F2 17 83 64 CB 01 FF FF FF FF FF FF FF 7F FF FF FF FF FF FF FF 7F 1D E4 43 03 81 64 CB 01 1D A4 AD 2D 4A 65 CB 01 FF FF FF FF FF FF FF 7F 1E 00 1E 00 04 00 02 00 00 00 00 00 08 00 02 00 00 00 00 00 0C 00 02 00 00 00 00 00 10 00 02 00 00 00 00 00 14 00 02 00 00 00 00 00 18 00 02 00 06 01 00 00 53 04 00 00 01 02 00 00 02 00 00 00 1C 00 02 00 20 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 1E 00 20 00 20 00 02 00 0C 00 0E 00 24 00 02 00 28 00 02 00 00 00 00 00 00 00 00 00 10 22 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0F 00 00 00 00 00 00 00 0F 00 00 00 63 00 6F 00 68 00 65 00 72 00 65 00 6E 00 63 00 65 00 73 00 65 00 72 00 76 00 65 00 72 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00

    00 00 00 00 00 00 00 00 - 00 00 00 00 02 00 00 00
    01 02 00 00 07 00 00 00 - 52 04 00 00 07 00 00 00

    10 00 00 00 00 00 00 00 - 0F 00 00 00 57 00 49 00
    4E 00 2D 00 49 00 50 00 - 56 00 4E 00 34 00 4D 00
    4F 00 4E 00 56 00 35 00 - 35 00 00 00 07 00 00 00
    00 00 00 00 06 00 00 00 - 4B 00 4E 00 49 00 47 00
    48 00 54 00 04 00 00 00 01 04 00 00 00 00 00 05 15 00 00 00 18 04 BB 68 3B 92 2C 10 D9 1E CB C1 80 6F 9C 55 83 64 CB 01 1E 00 63 00 6F 00 68 00 65 00 72 00 65 00 6E 00 63 00 65 00 73 00 65 00 72 00 76 00 65 00 72 00 34 00 10 00 14 00 48 00 00 00 00 00 00 00 00 00 63 00 6F 00 68 00 65 00 72 00 65 00 6E 00 63 00 65 00 73 00 65 00 72 00 76 00 65 00 72 00 40 00 6B 00 6E 00 69 00 67 00 68 00 74 00 2E 00 63 00 6F 00 6D 00 00 00 00 00 4B 00 4E 00 49 00 47 00 48 00 54 00 2E 00 43 00 4F 00 4D 00 00 00 00 00 76 FF FF FF 50 22 AC 87 78 B7 7D EF 24 8D C1 8B D8 1B 41 F8 00 00 00 00 76 FF FF FF 12 2E 86 1A 37 DF 6F 86 35 D9 E6 18 72 25 51 66 00 00 00 00
    


 */
public class ActiveDirectoryTest {

    private Subject subjectCacheServer;
    private String spn = "coherenceserver@KNIGHT.COM";

//    @Before
//    public void initialiseSubjects() throws Exception {
//        subjectCacheServer = JaasHelper.logon("com.sun.security.jgss.accept", "coherenceserver@KNIGHT.COM", "Secret1");
//    }

    @Test
    public void shouldDoNothing() throws Exception {
        
    }


    //@Test
    public void testLogin() throws Exception {
        byte[] ticket = KrbHelper.getServiceTicket(subjectCacheServer, spn);
        StringWriter stringWriter = new StringWriter();
        Base64OutputStream stream = new Base64OutputStream(stringWriter);
        stream.write(ticket);

        System.err.println("Ticket>>>>>");
        System.err.println(stringWriter.toString());
        System.err.println("<<<<<<<");
        stream.close();

        stream = new Base64OutputStream(new FileWriter("ad-base64-ticket.txt"));
        stream.write(ticket);
        stream.close();

        KrbTicket decodedTicket = KrbHelper.validate(ticket, true, subjectCacheServer);
        Pac pac = decodedTicket.getPAC();
        PacLogonInfo logonInfo = (PacLogonInfo) pac.getBuffer(PacBufferType.PAC_LOGON_INFO);
        System.err.println(logonInfo.getGroupSids());

//        stream = new Base64OutputStream(new FileWriter("ad-base64-pac.txt"));
//        stream.write(pacData);
//        stream.close();
//
//        PacLogonInfo logonInfo = null;
//        KerberosToken kerberosToken = new KerberosToken(ticket);
//        List<KerberosAuthData> authList = kerberosToken.getApRequest().getTicket().getEncData().getUserAuthorizations();
//        for (KerberosAuthData authData : authList) {
//            if (authData instanceof KerberosPacAuthData) {
//                org.jaaslounge.decoding.pac.Pac pac = ((KerberosPacAuthData) authData).getPac();
//                logonInfo = pac.getLogonInfo();
//            }
//        }
//
//        Pac pac = new Pac(pacData);
//        PacLogonInfo pacLogonInfo = (PacLogonInfo) pac.getBuffer(PacBufferType.PAC_LOGON_INFO);
//        PacSid[] groups = pacLogonInfo.getGroupSids();
//        for (PacSid p : groups) {
//            System.err.println(p);
//        }
//
//        org.jaaslounge.decoding.pac.PacSid[] jaasGroups = logonInfo.getGroupSids();
//        for (org.jaaslounge.decoding.pac.PacSid p :jaasGroups) {
//            System.err.println(sidString(p));
//        }

        System.err.println("Done");
    }

//    String sidString(org.jaaslounge.decoding.pac.PacSid sid) {
//        byte[] bytes = sid.getBytes();
//
//        StringBuilder string = new StringBuilder("S-");
//        string.append(bytes[0])
//                .append('-');
//
//        if (bytes[2] == 0x00 && bytes[3] == 0x00) {
//            long authorityValue = longFromBytes(bytes[4], bytes[5], bytes[6], bytes[7]);
//            string.append(authorityValue);
////        } else {
////            string.append("0x");
////            for (byte b : authority) {
////                string.append(HEX_CHAR_TABLE[b >> 4]);
////                string.append(HEX_CHAR_TABLE[b & 0x0f]);
////            }
//        }
//
//        byte subCount = bytes[1];
//        int idx = 8;
//        for (int i=0; i<subCount; i++) {
//            long sub = longFromBytes(bytes[idx+3],bytes[idx+2],bytes[idx+1],bytes[idx]);
//            idx +=4;
//            string.append('-').append(sub);
//        }
//        return string.toString();
//    }

    long longFromBytes(byte... bytes) {
        long auth = ((int)bytes[0] & 0xff);
        for (int i=1; i<bytes.length; i++) {
            auth <<= 8;
            auth += ((int)bytes[i] & 0xff);
        }
        return auth;
    }
    byte[] parseAuthData(byte[] token, int requiredType) throws Exception {
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

    public int getLength(int lenByte, ByteBuffer in) throws IOException {
        int value;
        int tmp;

        tmp = lenByte;
        if ((tmp & 0x080) == 0x00) {
            value = tmp;
        } else {
            tmp &= 0x07f;

            if (tmp == 0) {
                value = -1;
            } else if (tmp < 0 || tmp > 4) {
                throw new IOException("Incorrect length");
            } else {
                for (value = 0; tmp > 0; tmp--) {
                    value <<= 8;
                    value += 0x0ff & in.get();
                }
            }
        }

        return value;
    }

}
