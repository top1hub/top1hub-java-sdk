package top1hub;

import top1hub.message.Result;

/**
 * Created by Administrator on 2015/8/6.
 */
public class Demo {
    public static void main(String[] args) {
        ResourceManager mgr = ResourceManager.create().safeCode("输入安全码").container("容器名称").build();
        // 上传文件
        Result result = mgr.upload("文件URL", "文件路径");
        System.out.println(result.success());

        // 删除文件
        result = mgr.delete("文件URL");
        System.out.println(result.success());
    }
}
