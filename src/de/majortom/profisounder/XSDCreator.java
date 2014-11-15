package de.majortom.profisounder;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import de.majortom.profisounder.thundersound.config.SounderConfig;

public class XSDCreator {
	private static class PSSchemaOutputResolver extends SchemaOutputResolver {
		String schemaFileName;

		public PSSchemaOutputResolver(String schemaFileName) {
			this.schemaFileName = schemaFileName;
		}

		@Override
		public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
			File file = new File(schemaFileName);

			StreamResult result = new StreamResult(file);
			result.setSystemId(file.toURI().toURL().toString());
			return result;
		}

	}

	public static void main(String[] args) {
		JAXBContext context;
		try {
			context = JAXBContext.newInstance(SounderConfig.class);

			SchemaOutputResolver sor = new PSSchemaOutputResolver("config_schema.xsd");
			context.generateSchema(sor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
