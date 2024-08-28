package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.jcraft.jsch.SftpATTRS;

/**
 * Created by ekonsla on 21/01/2016.
 */
public class FileInfo {

    private final SftpATTRS sftpATTRS;

    public FileInfo(SftpATTRS sftpATTRS) {
        this.sftpATTRS = sftpATTRS;
    }

    /**
     * Returns permission in octal format e.g 755
     * @return int of octal permissions
     */
    public int getPermissionsOctal(){
        String octal = Integer.toOctalString(sftpATTRS.getPermissions());
        return Integer.valueOf(octal.substring(octal.length() - 3 , octal.length()));
    }

    /**
     * Gets permissions in decimal format
     * @see com.jcraft.jsch.SftpATTRS#getPermissions
     *
     * @return decimal permissions e.g 33279
     */
    public int getPermissions(){
        return sftpATTRS.getPermissions();
    }

    /**
     * Gets ATime
     * @see com.jcraft.jsch.SftpATTRS#getATime
     *
     * @return ATime
     */
    public int getATime() {
        return sftpATTRS.getATime();
    }

    /**
     * Gets MTime
     * @see com.jcraft.jsch.SftpATTRS#getMTime
     *
     * @return MTime
     */
    public int getMTime() {
        return sftpATTRS.getMTime();
    }

    /**
     * Gets extended
     * @see com.jcraft.jsch.SftpATTRS#getExtended
     *
     * @return extended
     */
    public String[] getExtended() {
        return sftpATTRS.getExtended();
    }

    /**
     * Get permissions in 'drwxrwxrwx' format
     * @see com.jcraft.jsch.SftpATTRS#getPermissionsString
     *
     * @return permissions
     */
    public String getPermissionsString(){
        return sftpATTRS.getPermissionsString();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isBlk
     * @return is blk
     */
    public boolean isBlk() {
        return sftpATTRS.isBlk();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getAtimeString
     * @return
     */
    public String getAtimeString() {
        return sftpATTRS.getAtimeString();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getMtimeString
     * @return
     */
    public String getMtimeString() {
        return sftpATTRS.getMtimeString();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isReg
     * @return
     */
    public boolean isReg() {
        return sftpATTRS.isReg();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isDir
     * @return
     */
    public boolean isDir() {
        return sftpATTRS.isDir();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isChr
     * @return
     */
    public boolean isChr() {
        return sftpATTRS.isChr();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isFifo
     * @return
     */
    public boolean isFifo() {
        return sftpATTRS.isFifo();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isLink
     * @return
     */
    public boolean isLink() {
        return sftpATTRS.isLink();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#isSock
     * @return
     */
    public boolean isSock() {
        return sftpATTRS.isSock();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getFlags
     * @return
     */
    public int getFlags() {
        return sftpATTRS.getFlags();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getSize
     * @return
     */
    public long getSize() {
        return sftpATTRS.getSize();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getUId
     * @return
     */
    public int getUId() {
        return sftpATTRS.getUId();
    }

    /**
     * @see com.jcraft.jsch.SftpATTRS#getGId
     * @return
     */
    public int getGId() {
        return sftpATTRS.getGId();
    }
}
