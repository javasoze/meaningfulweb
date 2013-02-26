What is Meaningful Web?
=======================

We aim to extract structured information from a web resource:

url --> meaningfulweb engine --> structured information

### Homepage:

[http://www.meaningfulweb.org](http://www.meaningfulweb.org)

### Artifacts:

1. meaningfulweb-opengraph.jar <- open graph parser
2. meaningfulweb-core.jar <-- core engine
3. meaningfulweb-app.war  <-- web application

### Build:

Build and release are managed via Maven: [http://maven.apache.org/](http://maven.apache.org/)

1. build all: under meaningfulweb, do: mvn clean install
2. start webapp: under meaningfulweb-app/, do: mvn jetty:run

application should be running at: [http://localhost:8080/](http://localhost:8080/)

the rest service should be running at: [http://localhost:8080/get-meaning?url=xxx](http://localhost:8080/get-meaning?url=xxx)

Example:

[http://localhost:8080/get-meaning?url=http://www.google.com](http://localhost:8080/get-meaning?url=http://www.google.com)


### Sample Code:

    // extract the best image representing an url

    String url = "http://www.google.com"

    MetaContentExtractor extractor = new MetaContentExtractor();
	MeaningfulWebObject obj = extractor.extractFromUrl(url);
	
    String bestImageURL = obj.getImage();
    String title = obj.getTitle();
    String description = obj.getDescription();
    String domain = obj.getDomain();

    ...


### Bugs:

File bugs [here](https://github.com/javasoze/meaningfulweb/issues?page=1&state=open)


