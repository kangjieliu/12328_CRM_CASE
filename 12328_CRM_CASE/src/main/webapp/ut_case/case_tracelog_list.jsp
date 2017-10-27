<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <title>处警单相关信息</title>
      <%@ include file="/ut_controls/PageHeaderMeta.jspf" %>
      
      <script type="text/javascript" language="javascript">
        //peng 20140620 处理页面的查询功能

    </script>

</head>
<body>
     <form id="form1" method="post">
		<div id="divBody" align="center">
			<div id="divContent">
				<div>${dgvList}</div>
			</div>
		</div>
	</form>
</body>
</html>
