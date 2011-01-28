<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN"
"http://www.w3.org/TR/html4/strict.dtd">

<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="assets" uri="http://www.igfoo.com/jsp/taglib/igfoo-assets.tld" %>

<html>
  <head>
    <assets:include types="title,meta,links" />
  </head>
  <body>
    <tiles:insertAttribute name="header" />
    <div id="ig_content_cnt">
      <tiles:insertAttribute name="content" />
    </div>
    <tiles:insertAttribute name="footer" />
    <assets:include types="scripts" />
  </body>
</html>
