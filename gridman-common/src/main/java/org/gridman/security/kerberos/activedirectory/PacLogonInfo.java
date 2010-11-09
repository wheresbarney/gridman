package org.gridman.security.kerberos.activedirectory;

import org.gridman.encoding.ndr.NDRSerializable;
import org.gridman.encoding.ndr.NDRStream;
import org.gridman.encoding.ndr.NDRWriter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents a Microsoft KERB_VALIDATION_INFO structure which is part of
 * an Active Directory PAC.
 *
 * @author Jonathan Knight
 *
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc237948(v=PROT.10).aspx">KERB_VALIDATION_INFO structure</a>
 * @see <a href="http://msdn.microsoft.com/en-us/library/cc230324(v=PROT.10).aspx">
 *        [MS-PAC]: Privilege Attribute Certificate Data Structure
 *      </a>
 */
public class PacLogonInfo implements NDRSerializable {
    public static final int USERFLAGS_GUEST = 1;
    public static final int USERFLAGS_NO_ENCRYPTION = 2;
    public static final int USERFLAGS_LAN_MANAGER_AUTH = 8;
    public static final int USERFLAGS_HAS_EXTRA_SIDS = 32;
    public static final int USERFLAGS_SUB_AUTH_USED = 64;
    public static final int USERFLAGS_MACHINE_ACCOUNT = 128;
    public static final int USERFLAGS_UNDERSTANDS_NTLMv2 = 256;
    public static final int USERFLAGS_HAS_RESOURCE_GROUPS = 512;
    public static final int USERFLAGS_HAS_PROFILE_PATH = 1024;
    public static final int USERFLAGS_NtChallengeResponseFields_USED = 2048;
    public static final int USERFLAGS_LmChallengeResponseFields_USED = 4096;
    public static final int USERFLAGS_LmAndNt_ChallengeResponseFields_USED = 8192;

    /**
     * LogonTime:  A FILETIME structure that contains the user account's lastLogon attribute
     * ([MS-ADA1] section <a href="http://msdn.microsoft.com/en-us/library/cc220051(v=PROT.10).aspx">2.351</a>) value.
     */
    private FileTime logonTime;
    /**
     * LogoffTime:  A FILETIME structure that contains the time the client's logon session should expire.
     * If the session should not expire, this structure SHOULD have the dwHighDateTime member set to 0x7FFFFFFF and
     * the dwLowDateTime member set to 0xFFFFFFFF.
     * A recipient of the PAC SHOULD <a href="http://msdn.microsoft.com/en-us/library/a1c36b00-1fca-415c-a4ca-e66e98844760(v=PROT.10)#id3">&lt;3&gt;</a>
     * use this value as an indicator of when to warn the user that the allowed time is due to expire.
     */
    private FileTime logoffTime;
    /**
     * KickOffTime:  A FILETIME structure that contains LogoffTime minus the user account's forceLogoff attribute
     * ([MS-ADA1] section <a href="http://msdn.microsoft.com/en-us/library/cc219903(v=PROT.10).aspx">2.233</a>) value.
     * If the client should not be logged off, this structure SHOULD have the dwHighDateTime member set to 0x7FFFFFFF
     * and the dwLowDateTime member set to 0xFFFFFFFF. The Kerberos service ticket end time is a replacement for
     * KickOffTime. The service ticket lifetime SHOULD NOT be set longer than the KickOffTime of an account.
     * A recipient of the PAC SHOULD <a href="http://msdn.microsoft.com/en-us/library/a1c36b00-1fca-415c-a4ca-e66e98844760(v=PROT.10)#id4">&lt;4&gt;</a>
     * use this value as the indicator of when the client should be forcibly disconnected.
     */
    private FileTime kickOffTime;
    /**
     * PasswordLastSet:  A FILETIME structure that contains the user account's pwdLastSet attribute
     * ([MS-ADA3] section <a href="http://msdn.microsoft.com/en-us/library/cc220785(v=PROT.10).aspx">2.174</a>) value.
     * If the password was never set, this structure MUST have the dwHighDateTime member set to 0x00000000 and the
     * dwLowDateTime member set to 0x00000000.
     */
    private FileTime passwordLastSet;
    /**
     * PasswordCanChange:  A FILETIME structure that contains the time at which the client's password is allowed to
     * change. If there is no restriction on when the client may change the password, this member MUST be set to zero.
     */
    private FileTime passwordCanChange;
    /**
     * PasswordMustChange:  A FILETIME structure that contains the time at which the client's password expires.
     * If the password will not expire, this structure MUST have the dwHighDateTime member set to 0x7FFFFFFF and
     * the dwLowDateTime member set to 0xFFFFFFFF.
     */
    private FileTime passwordMustChange;
    /**
     * LogonCount:  A 16-bit unsigned integer that contains the user account's LogonCount attribute ([MS-ADA1] section
     * <a href="http://msdn.microsoft.com/en-us/library/cc220078(v=PROT.10).aspx">2.375</a>) value.
     */
    private short logonCount;
    /**
     * BadPasswordCount:  A 16-bit unsigned integer that contains the user account's badPwdCount attribute ([MS-ADA1]
     * section <a href="http://msdn.microsoft.com/en-us/library/cc220131(v=PROT.10).aspx">2.83</a>) value for
     * interactive logon and SHOULD be zero for network logon t.
     */
    private short badPasswordCount;
    /**
     * EffectiveName:  A RPC_UNICODE_STRING structure that contains the user account's samAccountName attribute
     * ([MS-ADA3] section <a href="http://msdn.microsoft.com/en-us/library/cc220838(v=PROT.10).aspx">2.221</a>) value.
     */
    private RpcUnicodeString effectiveName;
    /**
     * FullName:  A RPC_UNICODE_STRING structure that contains the user account's full name for interactive logon
     * and SHOULD be zero for network logon. If FullName is omitted, this member MUST contain a RPC_UNICODE_STRING
     * structure with the Length member set to zero.
     */
    private RpcUnicodeString fullName;
    /**
     * LogonScript:  A RPC_UNICODE_STRING structure that contains the user account's scriptPath attribute ([MS-ADA3]
     * section <a href="http://msdn.microsoft.com/en-us/library/cc220849(v=PROT.10).aspx">2.231</a>) value for
     * interactive logon and SHOULD be zero for network logon. If no LogonScript is configured for the user,
     * this member MUST contain a RPC_UNICODE_STRING structure with the Length member set to zero.
     */
    private RpcUnicodeString logonScript;
    /**
     * ProfilePath:  A RPC_UNICODE_STRING structure that contains the user account's profilePath attribute ([MS-ADA3]
     * section <a href="http://msdn.microsoft.com/en-us/library/cc220775(v=PROT.10).aspx">2.166</a>) value for interactive
     *  logon and SHOULD be zero for network logon. If no ProfilePath is configured for the user, this member MUST contain
     * a RPC_UNICODE_STRING structure with the Length member set to zero.
     */
    private RpcUnicodeString profilePath;
    /**
     * HomeDirectory:  A RPC_UNICODE_STRING structure that contains the user account's HomeDirectory attribute
     * ([MS-ADA1] section <a href="http://msdn.microsoft.com/en-us/library/cc219970(v=PROT.10).aspx">2.295</a>)
     * value for interactive logon and SHOULD be zero for network logon. If no HomeDirectory is configured for the
     * user, this member MUST contain a RPC_UNICODE_STRING structure with the Length member set to zero.
     */
    private RpcUnicodeString homeDirectory;
    /**
     * HomeDirectoryDrive:  A RPC_UNICODE_STRING structure that contains the user account's HomeDrive attribute
     * ([MS-ADA1] section <a href="http://msdn.microsoft.com/en-us/library/cc219971(v=PROT.10).aspx">2.296</a>)
     * value for interactive logon and SHOULD be zero for network logon . This member MUST be populated if
     * HomeDirectory contains a <a href="http://msdn.microsoft.com/en-us/library/513bb0aa-c8d8-456f-8a31-4e200ca7970c(v=PROT.10)#uncpath">UNC path</a>.
     * If no HomeDirectoryDrive is configured for the user, this member MUST contain a RPC_UNICODE_STRING structure with the Length
     * member set to zero.
     */
    private RpcUnicodeString homeDirectoryDrive;
    /**
     * LogonServer: A RPC_UNICODE_STRING structure that contains the NetBIOS name of the Kerberos KDC that performed
     * the authentication server (AS) ticket request.
     */
    private RpcUnicodeString serverName;
    /**
     * A RPC_UNICODE_STRING structure that contains the NetBIOS name of the domain to which this account belongs.
     */
    private RpcUnicodeString domainName;
    /**
     * UserId:  A 32-bit unsigned integer that contains the
     * <a href="http://msdn.microsoft.com/en-us/library/07f3a42b-3466-41ab-8f60-e706e50d897a(v=PROT.10)#relative_identifier">RID</a>
     * of the account.
     * If the UserId member equals 0x00000000, the first group
     * <a href="http://msdn.microsoft.com/en-us/library/54af12e1-fcc1-4d62-bd47-c80514ac2615(v=PROT.10)#sid">SID</a>
     * in this member is the SID for this account
     */
    private PacSid userSid;
    /**
     * PrimaryGroupId:  A 32-bit unsigned integer that contains the RID for the primary group to which this
     * account belongs.
     */
    private PacSid groupSid;
    /**
     * GroupIds:  A pointer to a list of
     * <a href="http://msdn.microsoft.com/en-us/library/cc237945(v=PROT.10).aspx">GROUP_MEMBERSHIP (section </a>
     * <a href="http://msdn.microsoft.com/en-us/library/cc237945(v=PROT.10).aspx">2.2.2</a>
     * <a href="http://msdn.microsoft.com/en-us/library/cc237945(v=PROT.10).aspx">)</a> structures
     * that contains the groups to which the account belongs in the account domain. The number of groups
     * in this list MUST be equal to GroupCount.
     * GroupCount:  A 32-bit unsigned integer that contains the number of groups within the account domain to which
     * the account belongs.
     */
    private Set<PacSid> groupSids;
    /**
     */
    private PacSid[] resourceGroupSids;
    /**
     */
    private PacSid[] extraSids;
    /**
     */
    private int userAccountControl;
    /**
     * UserFlags:  A 32-bit unsigned integer that contains a set of bit flags that describe the user's logon information.
     */
    private int userFlags;
    /**
     */
    private long subAuthStatus;
    /**
     */
    private FileTime lastSuccessfulInteractiveLogonTime;
    /**
     */
    private FileTime lastFailedInteractiveLogonTime;

    public PacLogonInfo() {
    }

    public Set<PacSid> getGroupSids() {
        return Collections.unmodifiableSet(groupSids);
    }

    public boolean isInGroup(String groupSid) {
        for (PacSid group : this.groupSids) {
            if (groupSid.equals(group.toString())) {
                return true;
            }
        }
        return false;
    }

    public void deserialize(NDRStream stream) throws IOException {
        stream.skipBytes(20);
        logonTime = readFileTime(stream);
        logoffTime = readFileTime(stream);
        kickOffTime = readFileTime(stream);
        passwordLastSet = readFileTime(stream);
        passwordCanChange = readFileTime(stream);
        passwordMustChange = readFileTime(stream);
        effectiveName = readRpcUnicodeString(stream);
        fullName = readRpcUnicodeString(stream);
        logonScript = readRpcUnicodeString(stream);
        profilePath = readRpcUnicodeString(stream);
        homeDirectory = readRpcUnicodeString(stream);
        homeDirectoryDrive = readRpcUnicodeString(stream);
        logonCount = stream.readShort();
        badPasswordCount = stream.readShort();
        userSid = readPacSidFromRID(stream);
        groupSid = readPacSidFromRID(stream);

        int groupCount = stream.readInt();
        int groupPointer = stream.readInt();

        userFlags = stream.readInt();

        // skip the session key
        stream.skipBytes(16);

        serverName = readRpcUnicodeString(stream);
        domainName = readRpcUnicodeString(stream);

        int domainSidPointer = stream.readInt();

        // skip reserved 8 bytes
        stream.skipBytes(8);

        userAccountControl = stream.readInt();

        subAuthStatus = stream.readUnsignedInt();
        lastSuccessfulInteractiveLogonTime = readFileTime(stream);
        lastFailedInteractiveLogonTime = readFileTime(stream);

        // skip reserved 4 bytes
        stream.skipBytes(8);

        int extraSidCount = stream.readInt();
        int extraSidPointer = stream.readInt();
        int resourceDomainIdPointer = stream.readInt();
        int resourceGroupCount = stream.readInt();
        int resourceGroupPointer = stream.readInt();

        /*
          At this point we have read the fields from the KERB_VALIDATION_INFO structure
          According to the rules of NDR encoding the rest of the data in the stream is
          the data referenced by the embedded pointers.
         */

        effectiveName.deserializeString(stream);
        fullName.deserializeString(stream);
        logonScript.deserializeString(stream);
        profilePath.deserializeString(stream);
        homeDirectory.deserializeString(stream);
        homeDirectoryDrive.deserializeString(stream);

        // Read the PacSid AD Groups Array
        PacSid[] groups = new PacSid[0];
        if(groupPointer != 0) {
            int actualGroupCount = stream.readInt();
            if (groupCount != actualGroupCount) {
                throw new IOException("GroupCount does not equal actual serialized Group toByteArray count");
            }
            groups = new PacSid[groupCount];
            for(int i = 0; i < groupCount; i++) {
                groups[i] = new PacSid();
                groups[i].deserializeRIDAndAttributes(stream);
            }
        }

        serverName.deserializeString(stream);
        domainName.deserializeString(stream);

        PacSid domainSid = null;
        if (domainSidPointer != 0) {
            domainSid = new PacSid();
            domainSid.deserialize(stream);
        }

        this.groupSids = new HashSet<PacSid>(groupCount);
        for (int i=0; i<groupCount; i++) {
            this.groupSids.add(domainSid.mergeWithAttributes(groups[i]));
        }

        boolean hasExtraSids = (userFlags & USERFLAGS_HAS_EXTRA_SIDS) != 0;
        if (hasExtraSids && extraSidPointer != 0) {
            int actualCount = stream.readInt();
            if (actualCount != extraSidCount) {
                throw new IOException("Actual extra SID count did not match extra SID count attribute");
            }
            extraSids = new PacSid[extraSidCount];
            int[] sidPointers = new int[extraSidCount];
            int[] attributes = new int[extraSidCount];
            for(int i = 0; i < extraSidCount; i++) {
                sidPointers[i] = stream.readInt();
                attributes[i] = stream.readInt();
            }
            for(int i = 0; i < extraSidCount; i++) {
                if (sidPointers[i] != 0) {
                    extraSids[i] = new PacSid();
                    extraSids[i].deserialize(stream);
                    extraSids[i].setAttributes(attributes[i]);
                }
            }

        }

        PacSid resourceDomainSID = null;
        if (resourceDomainIdPointer != 0) {
            resourceDomainSID = new PacSid();
            resourceDomainSID.deserialize(stream);
        }

        boolean hasResourceGroups = (userFlags & USERFLAGS_HAS_RESOURCE_GROUPS) != 0;
        if (hasResourceGroups && resourceGroupPointer != 0) {
            int actualCount = stream.readInt();
            if (actualCount != resourceGroupCount) {
                throw new IOException("Actual resource group count did not match resource group count attribute");
            }
            resourceGroupSids = new PacSid[resourceGroupCount];
            for (int i=0; i<resourceGroupCount; i++) {
                PacSid sid = new PacSid();
                sid.deserialize(stream);
                sid.setAttributes(stream.readInt());
                resourceGroupSids[i] = resourceDomainSID.merge(sid);
            }
        }

        if (userSid.getSubCount() == 0 || userSid.hasAllZeroSubAuthorities()) {
            userSid = extraSids[0];
        } else {
            userSid = domainSid.merge(userSid);
        }
    }

    
    private PacSid readPacSidFromRID(NDRStream stream) throws IOException {
        PacSid sid = new PacSid();
        sid.deserializeRID(stream);
        return sid;
    }

    private FileTime readFileTime(NDRStream stream) throws IOException {
        FileTime time = new FileTime();
        time.deserialize(stream);
        return time;
    }

    private RpcUnicodeString readRpcUnicodeString(NDRStream stream) throws IOException {
        RpcUnicodeString string = new RpcUnicodeString();
        string.deserialize(stream);
        return string;
    }

    public void serialize(NDRWriter writer) throws IOException {
        writer.write(new byte[20]);
        logonTime.serialize(writer);
        logoffTime.serialize(writer);
        kickOffTime.serialize(writer);
        passwordLastSet.serialize(writer);
        passwordCanChange.serialize(writer);
        passwordMustChange.serialize(writer);
        effectiveName.serialize(writer);
        fullName.serialize(writer);
        logonScript.serialize(writer);
        profilePath.serialize(writer);
        homeDirectory.serialize(writer);
        homeDirectoryDrive.serialize(writer);
        writer.writeShort(logonCount);
        writer.writeShort(badPasswordCount);
        userSid.serializeRID(writer);
        groupSid.serializeRID(writer); 
    }

}
