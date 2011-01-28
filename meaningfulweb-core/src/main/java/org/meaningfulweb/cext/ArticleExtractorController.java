package org.meaningfulweb.cext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.meaningfulweb.util.http.HttpClientService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/extract/article.html")
public class ArticleExtractorController {

  private final static Logger LOG = LoggerFactory
    .getLogger(ArticleExtractorController.class);

  @Autowired
  @Qualifier("httpClientService")
  private HttpClientService httpClientService;

  @Autowired
  private HtmlExtractor htmlExtractor;

  @RequestMapping(method = RequestMethod.GET)
  public String getView(Model model) {
    ExtractForm extractForm = new ExtractForm();
    model.addAttribute("extractForm", extractForm);
    return "extract/article";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String extractArticle(Model model,
    @ModelAttribute ExtractForm extractForm, Errors errors,
    HttpServletRequest request, HttpServletResponse response) {

    String url = extractForm.getUrl();
    if (StringUtils.isNotBlank(url)) {

      // get the url content
      Map output = new HashMap();
      byte[] content;
      try {
        content = httpClientService.get(url);
      }
      catch (Exception e) {
        errors.reject("error.fetch.url");
        content = null;
      }

      if (content != null && content.length > 0) {

        // add the article processor
        List<String> components = extractForm.getComponents();
        components.add("article");

        Extract extract = new Extract();
        extract.setPipelines(extractForm.getPipelines());
        extract.setComponents(components);
        extract.setConfig(extractForm.getConfig());
        extract.setContent(content);
        extract.setMetadata(extractForm.getMetadata());

        try {
          htmlExtractor.extract(extract);
          output = extract.getExtracted();
          String articleHtml = (String)output.get("article.html");
          if (StringUtils.isNotBlank(articleHtml)) {
            model.addAttribute("html", articleHtml);
          }
        }
        catch (Exception e) {
          LOG.error("Error getting article content", e);
          errors.reject("error.extract");
        }
      }
    }

    return "extract/article";
  }
}
