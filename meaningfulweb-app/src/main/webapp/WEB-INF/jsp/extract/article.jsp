<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<div id="advsearch_header">
  <div id="search_wrapper">
    <span id="small_logo"><a href="/extract/article.html">
    <img width="82" height="47" src="/images/small_bitly_logotype.png"></a></span>
  </div>
</div>

<div id="ig_center_cnt">
  <form:form commandName="extractForm" action="/extract/article.html" method="post"> 
  <fieldset><legend>Extract Article from URL</legend>
  <div class="ig_fs_wrap">  
    <div class="ig_info">
      <h4>Extract Article</h4>
      <p>Use the form to the left to extract the article html from the url given.</p>
    </div>    
    <div class="ig_req">        
      <label for="url">URL</label>
      <span><form:input path="url" cssClass="txtvw" /></span>
      <span class="ig_extra">The url to extract the article form, starting with http://</span>          
      <form:errors path="url" cssClass="ig_err" />
    </div>       
    <div class="ig_submit buttons">
      <button type="submit" class="accept" name="_submit"> Extract Article</button>        
    </div>
  </div>
  </fieldset>  
  </form:form>
  
  <c:if test="${not empty html}">
  <div id="extracted">${html}</div>
  <div class="spaced"></div>
  <iframe src="${extractForm.url}" id="original"></iframe>
  </c:if>
  
  
  </div>
</div>

<div id="copy">&copy; 2010 Bit.ly<sup><small>TM</small></sup></div>