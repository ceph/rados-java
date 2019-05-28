/*
 * RADOS Java - Java bindings for librados
 *
 * Copyright (C) 2014 1&1 - Behar Veliqi <behar.veliqi@1und1.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package com.ceph.rados.exceptions;

/**
 * Contains all native error codes with appropriate names and messages.
 */
public enum ErrorCode {

    EPERM(-1, "Operation not permitted"),
    ENOENT(-2, "No such file or directory"),
    ESRCH(-3, "No such process"),
    EINTR(-4, "Interrupted system call"),
    EIO(-5, "I/O error"),
    ENXIO(-6, "No such device or address"),
    E2BIG(-7, "Argument list too long"),
    ENOEXEC(-8, "Exec format error"),
    EBADF(-9, "Bad file number"),
    ECHILD(-10, "No child processes"),
    EAGAIN(-11, "Try again"),
    ENOMEM(-12, "Out of memory"),
    EACCES(-13, "Permission denied"),
    EFAULT(-14, "Bad address"),
    ENOTBLK(-15, "Block device required"),
    EBUSY(-16, "Device or resource busy"),
    EEXIST(-17, "File exists"),
    EXDEV(-18, "Cross-device link"),
    ENODEV(-19, "No such device"),
    ENOTDIR(-20, "Not a directory"),
    EISDIR(-21, "Is a directory"),
    EINVAL(-22, "Invalid argument"),
    ENFILE(-23, "File table overflow"),
    EMFILE(-24, "Too many open files"),
    ENOTTY(-25, "Not a typewriter"),
    ETXTBSY(-26, "Text file busy"),
    EFBIG(-27, "File too large"),
    ENOSPC(-28, "No space left on device"),
    ESPIPE(-29, "Illegal seek"),
    EROFS(-30, "Read-only file system"),
    EMLINK(-31, "Too many links"),
    EPIPE(-32, "Broken pipe"),
    EDOM(-33, "Argument out of range"),
    ERANGE(-34, "Result too large"),
    EDEADLK(-35, "Resource deadlock would occur"),
    ENAMETOOLONG(-36, "Name too long"),
    ENOLCK(-37, "No record locks available"),
    ENOSYS(-38, "Function not implemented"),
    ENOTEMPTY(-39, "Directory not empty"),
    ELOOP(-40, "Too many symbolic links"),
    ENOMSG(-42, "No errorMessage of desired type"),
    EIDRM(-43, "Identifier removed"),
    ECHRNG(-44, "Channel number out of range"),
    EL2NSYNC(-45, "Level 2 not synchronized"),
    EL3HLT(-46, "Level 3 halted"),
    EL3RST(-47, "Level 3 reset"),
    ELNRNG(-48, "Link number out of range"),
    EUNATCH(-49, "Protocol driver not attached"),
    ENOCSI(-50, "No CSI structure available"),
    EL2HLT(-51, "Level 2 halted"),
    EBADE(-52, "Invalid exchange"),
    EBADR(-53, "Invalid request descriptor"),
    EXFULL(-54, "Exchange full"),
    ENOANO(-55, "No anode"),
    EBADRQC(-56, "Invalid request errorCode"),
    EBADSLT(-57, "Invalid slot"),
    EBFONT(-59, "Bad font file format"),
    ENOSTR(-60, "Not a stream"),
    ENODATA(-61, "No data available"),
    ETIME(-62, "Stream timeout"),
    ENOSR(-63, "Out of streams resources"),
    ENONET(-64, "Machine is not on the network"),
    ENOPKG(-65, "Package not installed"),
    EREMOTE(-66, "Object is remote"),
    ENOLINK(-67, "Link has been severed"),
    EADV(-68, "Advertise error"),
    ESRMNT(-69, "Srmount error"),
    ECOMM(-70, "Communication error on send"),
    EPROTO(-71, "Protocol error"),
    EMULTIHOP(-72, "Multihop attempted"),
    EDOTDOT(-73, "RFS specific error"),
    EBADMSG(-74, "Not a data errorMessage"),
    EOVERFLOW(-75, "Value too large for defined data type"),
    ENOTUNIQ(-76, "Name not unique on network"),
    EBADFD(-77, "File descriptor in bad state"),
    EREMCHG(-78, "Remote address changed"),
    ELIBACC(-79, "Can not access a needed shared library"),
    ELIBBAD(-80, "Accessing a corrupted shared library"),
    ELIBSCN(-81, ".lib section in a.out corrupted"),
    ELIBMAX(-82, "Attempting to link in too many shared libraries"),
    ELIBEXEC(-83, "Cannot exec a shared library directly"),
    EILSEQ(-84, "Illegal byte sequence"),
    ERESTART(-85, "Interrupted system call should be restarted"),
    ESTRPIPE(-86, "Streams pipe error"),
    EUSERS(-87, "Too many users"),
    ENOTSOCK(-88, "Socket operation on non-socket"),
    EDESTADDRREQ(-89, "Destination address required"),
    EMSGSIZE(-90, "errorMessage too long"),
    EPROTOTYPE(-91, "Protocol wrong type for socket"),
    ENOPROTOOPT(-92, "Protocol not available"),
    EPROTONOSUPPORT(-93, "Protocol not supported"),
    ESOCKTNOSUPPORT(-94, "Socket type not supported"),
    EOPNOTSUPP(-95, "Operation not supported on transport endpoint"),
    EPFNOSUPPORT(-96, "Protocol family not supported"),
    EAFNOSUPPORT(-97, "Address family not supported by protocol"),
    EADDRINUSE(-98, "Address already in use"),
    EADDRNOTAVAIL(-99, "Cannot assign requested address"),
    ENETDOWN(-100, "Network is down"),
    ENETUNREACH(-101, "Network is unreachable"),
    ENETRESET(-102, "Network dropped connection because of reset"),
    ECONNABORTED(-103, "Software caused connection abort"),
    ECONNRESET(-104, "Connection reset by peer"),
    ENOBUFS(-105, "No buffer space available"),
    EISCONN(-106, "Transport endpoint is already connected"),
    ENOTCONN(-107, "Transport endpoint is not connected"),
    ESHUTDOWN(-108, "Cannot send after transport endpoint shutdown"),
    ETOOMANYREFS(-109, "Too many references: cannot splice"),
    ETIMEDOUT(-110, "Connection timed out"),
    ECONNREFUSED(-111, "Connection refused"),
    EHOSTDOWN(-112, "Host is down"),
    EHOSTUNREACH(-113, "No route to host"),
    EALREADY(-114, "Operation already in progress"),
    EINPROGRESS(-115, "Operation now in progress"),
    ESTALE(-116, "Stale NFS file handle"),
    EUCLEAN(-117, "Structure needs cleaning"),
    ENOTNAM(-118, "Not a XENIX named type file"),
    ENAVAIL(-119, "No XENIX semaphores available"),
    EISNAM(-120, "Is a named type file"),
    EREMOTEIO(-121, "Remote I/O error"),
    EDQUOT(-122, "Quota exceeded"),
    ENOMEDIUM(-123, "No medium found"),
    EMEDIUMTYPE(-124, "Wrong medium type"),
    ECANCELED(-125, "Operation Canceled"),
    ENOKEY(-126, "Required key not available"),
    EKEYEXPIRED(-127, "Key has expired"),
    EKEYREVOKED(-128, "Key has been revoked"),
    EKEYREJECTED(-129, "Key was rejected by service"),
    EOWNERDEAD(-130, "Owner died"),
    ENOTRECOVERABLE(-131, "State not recoverable");

    private final int errorCode;
    private final String errorMessage;

    ErrorCode(int errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }


    /**
     * @param errorCode the error code
     * @return appropriate name of the error depending on the code
     */
    public static String getErrorName(int errorCode) {
        ErrorCode error = ErrorCode.getEnum(errorCode);
        if (error != null) {
            return error.name();
        }
        return "UNKNOWN_ERROR";
    }

    /**
     * @param errorCode the error code
     * @return appropriate message containing an explanation of the code
     */
    public static String getErrorMessage(int errorCode) {
        ErrorCode error = ErrorCode.getEnum(errorCode);
        if (error != null) {
            return error.getErrorMessage();
        }
        return String.format("Unknown error code: %d", errorCode);
    }

    /**
     * @param errorCode the error code
     * @return the ErrorCode enum representing the error code
     */
    public static ErrorCode getEnum(int errorCode) {
        for (ErrorCode value : values()) {
            if(value.errorCode == errorCode) {
                return value;
            }
        }
        return null;
    }
}
