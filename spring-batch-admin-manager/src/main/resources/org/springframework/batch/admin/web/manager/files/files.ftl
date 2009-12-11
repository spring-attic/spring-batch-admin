<#import "/spring.ftl" as spring />
<div id="configuration">

	<h2>Upload File</h2>

	<#if date??>
		<@spring.bind path="date" />
		<@spring.showErrors separator="<br/>" classOrStyle="error" /><br/>
	</#if>
	
	<p>
		Browse to a file containing input data and press the button marked Upload.
	</p>
	<#assign url><@spring.url relativeUrl="/batch/files"/></#assign>
	<form id="registerFileForm" action="${url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">

		<ol>
			<li><label for="filePath">Server Path</label><input id="path" type="text" name="path" /><li/>
			<li><label for="fileXml">Input File</label><input id="file" type="file" name="file" /></li>
		</ol>
		<input id="uploadFile" type="submit" value="Upload" name="uploadFile" />
		<!-- Spring JS does not support multipart forms so no Ajax here -->

	</form>
	
	<#if uploaded??>
	<p>Uploaded file: ${uploaded}</p>
	</#if>

	<#if files?? && files?size!=0>

		<p>Parent directory: ${outputDir}</p>
		<p>Trigger directory: ${triggerDir}</p>

		<#assign files_url><@spring.url relativeUrl="/batch/files"/></#assign>
		<form action="${files_url}" method="POST">
			<#if stoppedCount??>
				<p>Deleted ${deletedCount} files.</p>
			</#if>
			<input type="hidden" name="_method" value="DELETE"/>	
			<input id="delete" type="submit" value="Delete&nbsp;All" name="delete" />
		</form>
		
		<br/>

		<table title="Uploaded Files" class="bordered-table">
			<tr>
				<th>Name</th>
			</tr>
			<#list files as file>
				<#if file_index % 2 == 0>
					<#assign rowClass="name-sublevel1-even" />
				<#else>
					<#assign rowClass="name-sublevel1-odd" />
				</#if>
				<tr class="${rowClass}">
					<td>${file!}</td>
				</tr>
			</#list>
		</table>
		<#if startFile??>
			<ul class="controlLinks">
				<li>Rows: ${startFile}-${endFile} of ${totalFiles}</li> 
				<#if nextFile??><li><a href="${files_url}?startFile=${nextFile}&pageSize=${pageSize!20}">Next</a></li></#if>
				<#if previousFile??><li><a href="${files_url}?startFile=${previousFile}&pageSize=${pageSize!20}">Previous</a></li></#if>
				<!-- TODO: enable pageSize editing -->
				<li>Page Size: ${pageSize!20}</li>
			</ul>
		</#if>
		
	<#else>
		<p>There are no files to display.</p>
	</#if>

</div><!-- configuration -->