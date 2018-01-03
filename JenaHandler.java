package fyp;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;

public class JenaHandler {
	OntModel ontModel;
	
	JenaHandler(String filePath) {
		ontModel = ModelFactory.createOntologyModel();
		ontModel.read(filePath);
	}
	
	JenaHandler(OntModel ontModel) {
		this.ontModel = ontModel;
	}
}