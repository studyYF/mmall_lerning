<html>
<body>
<h2>Hello World!</h2>
<%--测试文件上传--%>
<form name="form1" action="/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="springmvc文件上传" />

</form>


<form name="form2" method="post" action="/manage/product/rich_text_upload.do" enctype="multipart/form-data">
    <input type="file" name="upload_file"/>
    <input type="submit" name="富文本图片上传"/>
</form>

</body>
</html>
