<#import "/spring.ftl" as spring />
<div id="configuration">

	<h2>Register XML File</h2>
	<p>
		Browse to a file containing Spring Batch XML configuration and press the button marked Upload.
	</p>
	<#assign url><@spring.url relativeUrl="${servletPath}/job-configuration"/></#assign>
	<form id="registerFileForm" action="${url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">

		<label for="fileXml">Job Configuration File</label><input id="fileXml" type="file" value="Upload" name="file" />
		<input id="registerFile" type="submit" value="Upload" name="registerFile" />
		<!-- Spring JS does not support multipart forms so no Ajax here -->

	</form>

</div><!-- configuration -->
