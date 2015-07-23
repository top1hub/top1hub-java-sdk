package top1hub;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import top1hub.message.Result;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class ResourceStorageTest {

    private static ResourceManager mgr;

    @BeforeClass
    public static void setUp() throws Exception {
        mgr = ResourceManager.create()
                .safeCode("")
                .container("test")
                .build();
    }

    @AfterClass
    public static void tearDown() {
        mgr.destory();
    }

    @Test
    public void checkAuth() {
        assertEquals(true, mgr.authorized());
    }

    @Test
    public void uploadPicture() throws IOException, URISyntaxException {
        Result result = mgr.upload("test" + Math.round(Math.random() * 10000) + ".doc", Paths.get("E:\\Download\\110105_dbzj.doc"));
        assertEquals(result.success(), true);
    }

    @Test
    public void detelePicture() {
        Result result = mgr.delete("showProduct.jpg");
        System.out.println("result = " + result);
        assertEquals(true, result.success());
    }

}