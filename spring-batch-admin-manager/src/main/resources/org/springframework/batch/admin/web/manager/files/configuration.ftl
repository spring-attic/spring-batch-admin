<#import "/spring.ftl" as spring />
<div id="configuration">

	<h2>Register XML File</h2>
	<p>
		Browse to a file containing Spring Batch XML configuration and press the button marked Upload.
	</p>
	<#assign url><@spring.url relativeUrl="${servletPath}/job-configuration-requests"/></#assign>
	<form id="registerFileForm" action="${url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">

		<label for="fileXml">Job Configuration File</label><input id="fileXml" type="file" value="Upload" name="file" />
		<input id="registerFile" type="submit" value="Upload" name="registerFile" />
		<!-- Spring JS does not support multipart forms so no Ajax here -->

	</form>

	<h2>Register XML Configuration</h2>
	<p>
		Paste in your configuration into the text area and press the button marked Register.
	</p>
	<#assign url><@spring.url relativeUrl="${servletPath}/job-configuration-requests"/></#assign>
	<form id="registerXmlForm" action="${url}" method="POST">

		<label for="textXml">Job Configuration XML</label><textarea id="textXml" name="xml"></textarea>
		<input id="registerXml" type="submit" value="Register" name="registerXml" />
		<script type="text/javascript">
			Spring.addDecoration(new Spring.AjaxEventDecoration({
				elementId : "registerXml",
				event : "onclick",
				formId: "registerXmlForm",
				params : {
					fragments: "body"
				}
			}));
		</script>
		<script type="text/javascript">
		    Spring.addDecoration(new Spring.ElementDecoration({
			elementId : "textXml",
			widgetType : "dijit.form.Textarea"
		    }));
		</script>

	</form>

</div><!-- configuration -->
