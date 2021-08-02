package de.unifrankfurt.informatik.acoli.fintan.genericIO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.shared.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.deri.tarql.CSVOptions;
import org.deri.tarql.InputStreamSource;
import org.deri.tarql.StreamingRDFWriter;
import org.deri.tarql.TarqlException;
import org.deri.tarql.TarqlParser;
import org.deri.tarql.TarqlQuery;
import org.deri.tarql.TarqlQueryExecution;
import org.deri.tarql.TarqlQueryExecutionFactory;
import org.deri.tarql.URLOptionsParser;

import de.unifrankfurt.informatik.acoli.fintan.core.FintanStreamComponent;
import de.unifrankfurt.informatik.acoli.fintan.core.StreamTransformerGenericIO;
import jena.cmd.ArgDecl;
import jena.cmd.CmdGeneral;

import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.serializer.FmtTemplate;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;

/**
 * Uses Tarql for segmented or unsegmented TSV/CSV streams
 * @author CF
 *
 */
public class TSV2TTLStreamTransformer extends StreamTransformerGenericIO {
	
	protected static final Logger LOG = LogManager.getLogger(TSV2TTLStreamTransformer.class.getName());


	private boolean segmented = false;

	private boolean hasHeaderRow = false;
	

	public boolean isSegmented() {
		return segmented;
	}

	public void setSegmented(boolean segmented) {
		this.segmented = segmented;
	}
	
	public boolean hasHeaderRow() {
		return hasHeaderRow;
	}

	public void setHasHeaderRow(boolean header_row) {
		this.hasHeaderRow = header_row;
	}


	private void processStream() {
		for (final String name:listInputStreamNames()) {
			if (getOutputStream(name) == null) continue;
			PrintStream out = new PrintStream(getOutputStream(name));
			
			String[] args = new String[] {};
			//TODO read args from config
			
			// for unsegmented streams, directly use TARQL processing
			if (!segmented) {
				InputStreamSource source = new InputStreamSource() {
					boolean open = false;
					public InputStream open() throws IOException {
						if (open) {
							throw new TarqlException("Cannot use Fintan stream in mapping requiring multiple read passes");
						}
						open = true;
						return getInputStream(name);
					}
				};
				new tarql(args, source, out).mainRun();
				continue;
			}
			
			// else
			
			// for segmented streams process tarql for individual segments
			BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(name)));
			String tsvsegment = "";
			String headerRow = null;
			try {
				for(String line = ""; line !=null; line=in.readLine()) {
					if (hasHeaderRow && headerRow == null) {
						// cache first line, if it is supposed to be the header_row
						headerRow=line;
					} else if (!line.equals(FintanStreamComponent.FINTAN_DEFAULT_SEGMENT_DELIMITER_TSV)) {
						// regular line
						tsvsegment+=line+"\n";
					} else {
						// end of segment
						// prepare input in case header row needs to be duplicated.
						if (hasHeaderRow) tsvsegment = headerRow+"\n"+tsvsegment;
						
						// print processed segment directly to outputstream
						new tarql(args, InputStreamSource.fromString(tsvsegment), out).mainRun();

						// print segment delimiter
						out.println(FintanStreamComponent.FINTAN_DEFAULT_SEGMENT_DELIMITER_TTL);
						
						// clear segment cache
						tsvsegment = "";
					}
				}
			} catch (IOException e) {
				LOG.error("Error when reading from Stream "+name+ ": " +e);
			}
		}
		
		
	}
	
	@Override
	public void start() {
		run();
	}

	@Override
	public void run() {
		try {
			processStream();
		} catch (Exception e) {
			LOG.error(e, e);
			System.exit(1);
		}
	}

	//------------------------------------------------------------------------//
	//------------------------------------------------------------------------//
	//------------------------------------------------------------------------//
	//---------------------------START NESTED CLASSES-------------------------//
	//------------------------------------------------------------------------//
	//------------------------------------------------------------------------//
	//------------------------------------------------------------------------//
	
	
	/**
	 * [deprecated][artifact]
	 * unable to properly override exec() function, 
	 * since declaration of parameters is private and 
	 * stream declaration is not separated from execution.
	 * @author CF
	 *
	 */
	private class tarqlFintan extends org.deri.tarql.tarql {

		private final ArgDecl fintanStreamArg = new ArgDecl(false, "fintan-stream");
		private boolean useFintanStream = false;
		

		public tarqlFintan(String[] args) {
			super(args);
			getUsage().startCategory("Fintan options");
			add(fintanStreamArg,         "--fintan-stream", "Read input from Fintan stream instead of file");
		}

		@Override
		protected void processModulesAndArgs() {
			super.processModulesAndArgs();

			if (hasArg(fintanStreamArg)) {
				useFintanStream = true;
			}
		}

		/**
		 *  unable to properly override exec() function, 
		 *  since declaration of parameters is private and 
		 *  stream declaration is not separated from execution.
		 */
		@Override
		protected void exec() {
			// TODO Auto-generated method stub
			super.exec();
		}
		
	}
	
	/**
	 * Copy of "The <code>tarql</code> CLI command."
	 * 
	 * Now supports Fintan Streams as I/O.
	 * 
	 * Should probably be properly extended from original tarql class. 
	 * TODO: discuss with Tarql-creators.
	 */
	private static class tarql extends CmdGeneral {

		// This will be displayed by --version
		public static final String VERSION;
		public static final String BUILD_DATE;
		
		public static final String NS = "http://tarql.github.io/tarql#";
		
		static {
			String version = "Unknown";
			String date = "Unknown";
			try {
				URL res = tarql.class.getResource(tarql.class.getSimpleName() + ".class");
				Manifest mf = ((JarURLConnection) res.openConnection()).getManifest();
				version = (String) mf.getMainAttributes().getValue("Implementation-Version");
				date = (String) mf.getMainAttributes().getValue("Built-Date")
						.replaceFirst("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)-(\\d\\d)(\\d\\d)", "$1-$2-$3T$4:$5:00Z");
		    } catch (Exception ex) {
			}
			VERSION = version;
			BUILD_DATE = date;
			
			TarqlQuery.registerFunctions();
		}
		
		public static void main(String... args) {
			new tarql(args, InputStreamSource.fromStdin(), System.out).mainRun();
		}
		
		//new
		private boolean useFintanStream = false;
		private final InputStreamSource fintanInputStream;
		private final PrintStream fintanOutputStream; //can be set to System.out, for default operations
		private final ArgDecl fintanStreamArg = new ArgDecl(false, "fintan-stream");
		//new_end
		
		
		private final ArgDecl stdinArg = new ArgDecl(false, "stdin");
		private final ArgDecl testQueryArg = new ArgDecl(false, "test");
		private final ArgDecl withHeaderArg = new ArgDecl(false, "header-row", "header");
		private final ArgDecl withoutHeaderArg = new ArgDecl(false, "no-header-row", "no-header", "H");
		private final ArgDecl encodingArg = new ArgDecl(true, "encoding", "e");
		private final ArgDecl nTriplesArg = new ArgDecl(false, "ntriples");
		private final ArgDecl delimiterArg = new ArgDecl(true, "delimiter", "d");
		private final ArgDecl tabsArg = new ArgDecl(false, "tabs", "tab", "t");
		private final ArgDecl quoteArg = new ArgDecl(true, "quotechar");
		private final ArgDecl escapeArg = new ArgDecl(true, "escapechar", "p");
		private final ArgDecl baseArg = new ArgDecl(true, "base");
		private final ArgDecl writeBaseArg = new ArgDecl(false, "write-base");
		private final ArgDecl dedupArg = new ArgDecl(true, "dedup");
		
		
		private String queryFile;
		private List<String> csvFiles = new ArrayList<String>();
		private boolean stdin = false;
		private CSVOptions options = new CSVOptions();
		private boolean testQuery = false;
		private boolean writeNTriples = false;
		private String baseIRI = null;
		private boolean writeBase = false;
		private int dedupWindowSize = 0;
		
		private ExtendedIterator<Triple> resultTripleIterator = NullIterator.instance();
		
		public tarql(String[] args, InputStreamSource fintanInputStream, PrintStream fintanOutputStream) {
			super(args);
			this.fintanInputStream = fintanInputStream;
			this.fintanOutputStream = fintanOutputStream;
			
			getUsage().startCategory("Output options");
			add(testQueryArg,     "--test", "Show CONSTRUCT template and first rows only (for query debugging)");
			add(writeBaseArg,     "--write-base", "Write @base if output is Turtle");
			add(nTriplesArg,      "--ntriples", "Write N-Triples instead of Turtle");
			add(dedupArg, "--dedup", "Window size in which to remove duplicate triples");

			getUsage().startCategory("Input options");
			add(fintanStreamArg,  "--fintan-stream", "Read input from Fintan stream instead of file");
			add(stdinArg,         "--stdin", "Read input from STDIN instead of file");
			add(delimiterArg,     "-d   --delimiter", "Delimiting character of the input file");
			add(tabsArg,          "-t   --tabs", "Specifies that the input is tab-separated (TSV)");
			add(quoteArg,         "--quotechar", "Quote character used in the input file, or \"none\"");
			add(escapeArg,        "-p   --escapechar", "Character used to escape quotes in the input file, or \"none\"");
			add(encodingArg,      "-e   --encoding", "Override input file encoding (e.g., utf-8 or latin-1)");
			add(withoutHeaderArg, "-H   --no-header-row", "Input file has no header row; use variable names ?a, ?b, ...");
			add(withHeaderArg,    "--header-row", "Input file's first row is a header with variable names (default)");
			add(baseArg,          "--base", "Base IRI for resolving relative IRIs");
			
			getUsage().startCategory("Main arguments");
			getUsage().addUsage("query.sparql", "File containing a SPARQL query to be applied to an input file");
			getUsage().addUsage("table.csv", "CSV/TSV file to be processed; can be omitted if specified in FROM clause");
			modVersion.addClass(tarql.class);
		}
		
		@Override
	    protected String getCommandName() {
			return Lib.className(this);
		}
		
		@Override
		protected String getSummary() {
			return getCommandName() + " [options] query.sparql [table.csv [...]]";
		}

		@Override
		protected void processModulesAndArgs() {
			if (getPositional().isEmpty()) {
				printHelp();
			}
			queryFile = getPositionalArg(0);
			for (int i = 1; i < getPositional().size(); i++) {
				csvFiles.add(getPositionalArg(i));
			}

			if (hasArg(stdinArg)) {
				stdin = true;
			}
			if (hasArg(withHeaderArg)) {
				options.setColumnNamesInFirstRow(true);
			}
			if (hasArg(withoutHeaderArg)) {
				options.setColumnNamesInFirstRow(false);
			}
			if (hasArg(testQueryArg)) {
				testQuery = true;
			}
			if (hasArg(encodingArg)) {
				options.setEncoding(getValue(encodingArg));
			}
			if (hasArg(nTriplesArg)) {
				writeNTriples = true;
			}
			if (hasArg(tabsArg)) {
				options.setDefaultsForTSV();
			} else {
				options.setDefaultsForCSV();
			}
			if (hasArg(delimiterArg)) {
				Character d = getCharValue(delimiterArg);
				if (d == null) {
					cmdError("Value of --delimiter must be a single character");
				}
				options.setDelimiter(d);
			}
			if (hasArg(quoteArg)) {
				options.setQuoteChar(getCharValue(quoteArg));
			}
			if (hasArg(escapeArg)) {
				options.setEscapeChar(getCharValue(escapeArg));
			}
			if (hasArg(baseArg)) {
				baseIRI = getValue(baseArg);
			}
			if (hasArg(writeBaseArg)) {
				writeBase = true;
			}
			if (hasArg(dedupArg)) {
				if (getValue(dedupArg) == null) {
					cmdError("--dedup needs an integer value");
				}
				try {
					dedupWindowSize = Integer.parseInt(getValue(dedupArg));
				} catch (NumberFormatException ex) {
					dedupWindowSize = -1;
				}
				if (dedupWindowSize < 0) {
					cmdError("Value of --dedup must be integer >= 0");
				}
			}
			
			//new -- Fintan Stream overwrites stdin, if both are specified
			if (hasArg(fintanStreamArg)) {
				useFintanStream = true;
				stdin = false;
			}
		}

		@Override
		protected void exec() {
			try {
				TarqlQuery q = baseIRI == null
						? new TarqlParser(queryFile).getResult()
						: new TarqlParser(queryFile, baseIRI).getResult();
				if (testQuery) {
					q.makeTest();
				}
				//new -- fintanStream takes priority
				if (useFintanStream) {
					processResults(TarqlQueryExecutionFactory.create(q, 
							fintanInputStream, options));
				} else if (stdin) {
					processResults(TarqlQueryExecutionFactory.create(q, 
							InputStreamSource.fromStdin(), options));
				} else if (csvFiles.isEmpty()) {
					processResults(TarqlQueryExecutionFactory.create(q, options));
				} else {
					for (String csvFile: csvFiles) {
						URLOptionsParser parseResult = new URLOptionsParser(csvFile);
						processResults(TarqlQueryExecutionFactory.create(q, 
								InputStreamSource.fromFilenameOrIRI(parseResult.getRemainingURL()), 
								parseResult.getOptions(options)));
					}
				}
				if (resultTripleIterator.hasNext()) {
					StreamingRDFWriter writer = new StreamingRDFWriter(fintanOutputStream, resultTripleIterator);
					writer.setDedupWindowSize(dedupWindowSize);
					if (writeNTriples) {
						writer.writeNTriples();
					} else {
						writer.writeTurtle(
								q.getPrologue().getBaseURI(),
								q.getPrologue().getPrefixMapping(), writeBase);
					}
				}
			} catch (NotFoundException ex) {
				error("Not found", ex);
			} catch (IOException ioe) {
				error("IOException", ioe);
			} catch (QueryParseException ex) {
				error("Error parsing SPARQL query", ex);
			} catch (TarqlException ex) {
				error(null, ex);
			}
		}

		private void error(String message, Throwable cause) {
			LOG.info(message == null ? "Error" : message, cause);
			if (message == null) {
				cmdError(cause.getMessage());
			} else {
				cmdError(message + ": " + cause.getMessage());
			}
		}
		
		private Character getCharValue(ArgDecl arg) {
			String value = getValue(arg);
			if (CSVOptions.charNames.containsKey(value)) {
				return CSVOptions.charNames.get(value);
			}
			if (value != null && value.length() == 1) {
				return value.charAt(0);
			}
			cmdError("Value of --" + arg.getKeyName() + " cannot be more than one character");
			return null;
		}
		
		private void processResults(TarqlQueryExecution ex) throws IOException {
			if (testQuery && ex.getFirstQuery().getConstructTemplate() != null) {
				IndentedWriter out = new IndentedWriter(fintanOutputStream); 
				new FmtTemplate(out, new SerializationContext(ex.getFirstQuery())).format(ex.getFirstQuery().getConstructTemplate());
				out.flush();
			}
			if (ex.getFirstQuery().isSelectType()) {
				fintanOutputStream.println(ResultSetFormatter.asText(ex.execSelect()));
			} else if (ex.getFirstQuery().isAskType()) {
				fintanOutputStream.println(ResultSetFormatter.asText(ex.execSelect()));
			} else if (ex.getFirstQuery().isConstructType()) {
				resultTripleIterator = resultTripleIterator.andThen(ex.execTriples());
			} else {
				cmdError("Only query forms CONSTRUCT, SELECT and ASK are supported");
			}
		}
		
	}

	
}