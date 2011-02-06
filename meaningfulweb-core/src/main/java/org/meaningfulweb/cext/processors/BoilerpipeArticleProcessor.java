package org.meaningfulweb.cext.processors;

import java.io.StringReader;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.meaningfulweb.cext.HtmlContentProcessor;
import org.xml.sax.InputSource;

import de.l3s.boilerpipe.document.TextDocument;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.ArticleSentencesExtractor;
import de.l3s.boilerpipe.extractors.ExtractorBase;
import de.l3s.boilerpipe.sax.BoilerpipeSAXInput;

public class BoilerpipeArticleProcessor extends HtmlContentProcessor {
	
	private boolean tuneForSentences = true;
	private ExtractorBase extractor = new ArticleSentencesExtractor();
	
	public boolean isTuneForSentences() {
	  return tuneForSentences;
	}

	public void setTuneForSentences(boolean tuneForSentences) {
	  this.tuneForSentences = tuneForSentences;
	  if (tuneForSentences){
		extractor = ArticleSentencesExtractor.INSTANCE;
	  }
	  else{
		extractor = ArticleExtractor.INSTANCE;
	  }
	}

	@Override
	public boolean processContent(Document document) {
		try{
		  XMLOutputter outputter = new XMLOutputter();
		  String xml = outputter.outputString(document);
		  BoilerpipeSAXInput saxinput = new BoilerpipeSAXInput(new InputSource(new StringReader(xml)));
		  TextDocument textDoc = saxinput.getTextDocument();
		  String text = extractor.getText(textDoc);
		  addExtractedValue("text", text);
		  return true;
		}
		catch(Exception e){
			return false;
		}
	}

}
