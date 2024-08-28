package com.ericsson.cifwk.taf.tools.cli.jsch;

import com.jcraft.jsch.SftpATTRS;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by ekonsla on 25/01/2016.
 */
public class FileInfoTest {

    @Test
    public void fileInfoTest(){
        SftpATTRS sftpATTRS = mock(SftpATTRS.class);
        String[] actualExtended = {"blah", "halb"};

        doReturn(33_279).when(sftpATTRS).getPermissions();
        doReturn("drwxrwxrwx").when(sftpATTRS).getPermissionsString();

        doReturn(666_777).when(sftpATTRS).getATime();
        doReturn(444_555).when(sftpATTRS).getMTime();
        doReturn("999").when(sftpATTRS).getAtimeString();
        doReturn("321").when(sftpATTRS).getMtimeString();

        doReturn(actualExtended).when(sftpATTRS).getExtended();

        doReturn(123).when(sftpATTRS).getFlags();
        doReturn(234L).when(sftpATTRS).getSize();
        doReturn(567).when(sftpATTRS).getGId();
        doReturn(87).when(sftpATTRS).getUId();

        FileInfo fileInfo = new FileInfo(sftpATTRS);

        assertEquals(777, fileInfo.getPermissionsOctal());

        assertEquals(33_279, fileInfo.getPermissions());
        assertEquals("drwxrwxrwx", fileInfo.getPermissionsString());
        assertEquals(666_777, fileInfo.getATime());

        assertEquals(444_555, fileInfo.getMTime());

        String[] expectedExtended = {"blah", "halb"};
        assertTrue(Arrays.equals(expectedExtended, actualExtended));

        assertEquals("999", fileInfo.getAtimeString());
        assertEquals("321", fileInfo.getMtimeString());

        assertEquals(123, fileInfo.getFlags());
        assertEquals(234L, fileInfo.getSize());
        assertEquals(567, fileInfo.getGId());
        assertEquals(87, fileInfo.getUId());
    }
}
