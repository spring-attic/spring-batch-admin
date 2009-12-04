<#import "/spring.ftl" as spring />
<div id="configuration">

	<h2>Upload File</h2>
	<p>
		Browse to a file containing input data and press the button marked Upload.
	</p>
	<#assign url><@spring.url relativeUrl="/batch/leads/files"/></#assign>
	<form id="registerFileForm" action="${url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">

		<label for="fileXml">Input File</label><input id="file" type="file" name="data" />
		<input id="uploadFile" type="submit" value="Upload" name="uploadFile" />
		<!-- Spring JS does not support multipart forms so no Ajax here -->

	</form>

</div><!-- configuration -->
