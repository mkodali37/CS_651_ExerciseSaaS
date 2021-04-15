<%@ page
	import="com.google.appengine.api.blobstore.BlobstoreServiceFactory"%>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService"%>


<%@ page import="com.google.cloud.vision.v1.EntityAnnotation"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="java.util.List"%>

<%
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<title>Labels</title>
</head>
<body>

	<table>
		<tr>
			<td align="center"><b> Label Detection using Google cloud vision
					</b></td>
		</tr>
		<tr>
			<td><br></td>
		</tr>

	</table>

	<table>
		<tr>
			<td>Uploaded image</td>
			<td></td>
			<td><img src="<%=request.getAttribute("imageUrl")%>"></td>
		</tr>
		<tr>
			<td><br></td>
		</tr>
		<tr>
			<td><br></td>
		</tr>
		<%
			List<EntityAnnotation> imageLabels = (List<EntityAnnotation>) request.getAttribute("imageLabels");
		%>
		<tr>
			<td>Labels from Google Cloud Vision</td>
			<td></td>
			<td>
				<table border="1">
					<tr>
						<td>Label</td>
						<td>Score</td>

					</tr>

					<c:forEach items="${imageLabels}" var="label">
						<tr>
							<td>${label.getDescription()}</td>
							<td>${label.getScore()}</td>
						</tr>
					</c:forEach>

				</table>

			</td>
		</tr>
		<tr>
			<td><a href="/">Reset Image</</a></td>
		</tr>
	</table>

</body>
</html>