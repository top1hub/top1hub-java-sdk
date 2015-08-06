# top1hub-java-sdk
源速SDK JAVA版
ResourceManager mgr = ResourceManager.create().safeCode("输入安全码").container("容器名称").build();
// 上传文件
Result result = mgr.upload("文件URL", "文件路径");
System.out.println(result.success());

// 删除文件
result = mgr.delete("文件URL");
System.out.println(result.success());