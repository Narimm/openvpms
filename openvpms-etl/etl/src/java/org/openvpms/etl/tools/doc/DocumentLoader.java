/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.stringparsers.BooleanStringParser;
import com.martiansoftware.jsap.stringparsers.FileStringParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Pattern;


/**
 * Loads documents from the file system, attaching them to existing document acts.
 * Documents may be loaded by name or by identifier.
 * <p/>
 * To load documents by name, the document file name is used to locate a document act with the same name.
 * <p/>
 * To load documents by identifier, the identifier is parsed from the document file name and used to retrieve the
 * corresponding document act.
 *
 * @author Tim Anderson
 */
public class DocumentLoader {

    /**
     * The document creator.
     */
    private final Loader loader;

    /**
     * Determines if the generator should fail on error.
     */
    private boolean failOnError = true;

    /**
     * The application context path.
     */
    private String contextPath;

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The logger.
     */
    static final Log log = LogFactory.getLog(DocumentLoader.class);

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";


    /**
     * Creates a new <tt>DocumentLoader</tt>.
     *
     * @param loader the loader to use
     */
    public DocumentLoader(Loader loader) {
        this.loader = loader;
    }

    /**
     * Creates a new <tt>DocumentLoader</tt> from command line arguments.
     *
     * @param args               the arguments to parse
     * @param service            the achetype service. If <tt>null</tt> it will be bootstrapped from the default
     *                           spring context
     * @param transactionManager the transaction manager. If <tt>null</tt> it will be bootstrapped from the default
     *                           spring context
     * @throws DocumentLoaderException if the arguments are invalid
     */
    protected DocumentLoader(String[] args, IArchetypeService service, PlatformTransactionManager transactionManager) {
        this(args, createParser(), service, transactionManager);
    }

    /**
     * Creates a new <tt>DocumentLoader</tt> from command line arguments.
     *
     * @param args   the arguments to parse
     * @param parser the argument parser
     * @throws DocumentLoaderException if the arguments are invalid
     */
    protected DocumentLoader(String[] args, JSAP parser) {
        this(args, parser, null, null);
    }

    /**
     * Creates a new <tt>DocumentLoader</tt> from command line arguments.
     *
     * @param args               the arguments to parse
     * @param parser             the argument parser
     * @param service            the achetype service. If <tt>null</tt> it will be bootstrapped from the default
     *                           spring context
     * @param transactionManager the transaction manager. If <tt>null</tt> it will be bootstrapped from the default
     *                           spring context
     * @throws DocumentLoaderException if the arguments are invalid
     */
    protected DocumentLoader(String[] args, JSAP parser, IArchetypeService service,
                             PlatformTransactionManager transactionManager) {
        JSAPResult config = parser.parse(args);
        boolean byId = config.getBoolean("byid");
        boolean byName = config.getBoolean("byname");
        if (!config.success()) {
            String message = null;
            Iterator iter = config.getErrorMessageIterator();
            if (iter.hasNext()) {
                message = iter.next().toString();
            }
            throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.InvalidArguments, message);
        } else if (!byId && !byName) {
            throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.InvalidArguments,
                                              "One of --byid or --byname must be specified");
        } else {
            File source = config.getFile("source");
            File target = config.getFile("dest");
            checkDirs(source, target);

            contextPath = config.getString("context");

            if (service == null) {
                service = (IArchetypeService) getContext().getBean("archetypeService");
            }
            DocumentFactory factory = new DefaultDocumentFactory();
            LoaderListener listener = (config.getBoolean("verbose")) ? new LoggingLoaderListener(log, target)
                                                                     : new DefaultLoaderListener(target);

            String type[] = config.getStringArray("type");
            if (byId) {
                boolean recurse = config.getBoolean("recurse");
                boolean overwrite = config.getBoolean("overwrite");
                String regexp = config.getString("regexp");
                Pattern pattern = Pattern.compile(regexp);
                if (transactionManager == null) {
                    transactionManager = (PlatformTransactionManager) getContext().getBean("txnManager");
                }
                loader = new IdLoader(source, type, service, factory, transactionManager, recurse, overwrite, pattern);
            } else {
                loader = new NameLoader(source, type, service, factory);
            }
            loader.setListener(listener);
            setFailOnError(config.getBoolean("failOnError"));
        }
    }

    /**
     * Determines if generation should fail when an error occurs.
     * Defaults to <tt>true</tt>.
     *
     * @param failOnError if <tt>true</tt> fail when an error occurs
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Loads documents.
     */
    public void load() {
        Date start = new Date();
        log.info("Starting load at: " + start);

        while (loader.hasNext()) {
            if (!loader.loadNext() && failOnError) {
                break;
            }
        }
        dumpStats(start);
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        JSAP parser = null;
        try {
            parser = createParser();
            DocumentLoader loader = new DocumentLoader(args, parser);
            loader.load();
        } catch (DocumentLoaderException exception) {
            if (exception.getMessage() != null) {
                System.err.println(exception.getMessage());
            }
            if (parser != null) {
                System.err.println("Usage: java " + DocumentLoader.class.getName());
                System.err.println("                " + parser.getUsage());
                System.err.println();
                System.err.println(parser.getHelp());
            } else {
                log.error(exception, exception);
            }
            System.exit(1);
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
            System.exit(1);
        }
    }

    /**
     * Returns the application context, creating it if necessary.
     *
     * @return the application context
     */
    private ApplicationContext getContext() {
        if (context == null) {
            if (!new File(contextPath).exists()) {
                context = new ClassPathXmlApplicationContext(contextPath);
            } else {
                context = new FileSystemXmlApplicationContext(contextPath);
            }
        }
        return context;
    }

    /**
     * Verifies that the source and target directories are valid.
     *
     * @param source the source directory
     * @param target the target directory. May be <tt>null</tt>
     */
    private void checkDirs(File source, File target) {
        if (source == null) {
            throw new IllegalArgumentException("Argument 'source' is null");
        }
        if (target != null) {
            if (target.equals(source)) {
                throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.SourceTargetSame);

            }
            File parent = target.getParentFile();
            while (parent != null) {
                if (parent.equals(source)) {
                    throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.TargetChildOfSource);
                }
                parent = parent.getParentFile();
            }
        }
    }

    /**
     * Dumps statistics.
     *
     * @param start the start time
     */
    private void dumpStats(Date start) {
        Date end = new Date();
        log.info("Ending load at: " + end);

        double elapsed = (end.getTime() - start.getTime()) / 1000;
        LoaderListener listener = loader.getListener();
        int total = listener.getProcessed();
        double rate = (elapsed != 0) ? total / elapsed : 0;
        log.info("Loaded: " + listener.getLoaded());
        log.info("Errors: " + listener.getErrors());
        log.info("Total:  " + total);
        log.info(String.format("Processed %d files in %.2f seconds (%.2f files/sec)", total, elapsed, rate));
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     */
    private static JSAP createParser() {
        JSAP parser = new JSAP();
        FileStringParser dirParser = FileStringParser.getParser();
        dirParser.setMustBeDirectory(true);
        dirParser.setMustExist(true);
        try {
            parser.registerParameter(
                    new Switch("byid").setShortFlag('i')
                                             .setLongFlag("byid")
                                             .setHelp("Load files using the identifiers in their names"));
            parser.registerParameter(new Switch("byname").setShortFlag('n')
                                             .setLongFlag("byname")
                                             .setHelp("Load files by matching their names with document acts"));
            parser.registerParameter(new FlaggedOption("source").setShortFlag('s')
                                             .setLongFlag("source")
                                             .setStringParser(dirParser)
                                             .setDefault("./")
                                             .setHelp("The directory to load files from. "));
            parser.registerParameter(new Switch("recurse").setShortFlag('r')
                                             .setLongFlag("recurse")
                                             .setDefault("false")
                                             .setHelp("Recursively scan the source directory"));
            parser.registerParameter(new Switch("overwrite").setShortFlag('o')
                                             .setLongFlag("overwrite")
                                             .setDefault("false")
                                             .setHelp("Overwrite existing attachments. Ony applies when --byid is used"));
            parser.registerParameter(new FlaggedOption("regexp")
                                             .setLongFlag("regexp")
                                             .setDefault(IdLoader.DEFAULT_REGEXP)
                                             .setHelp("Regular expression for parsing identifiers from file names. "
                                                      + "Only applies when --byid is used"));
            parser.registerParameter(new FlaggedOption("dest").setShortFlag('d')
                                             .setLongFlag("dest")
                                             .setStringParser(dirParser)
                                             .setHelp("The directory to move files to on successful load."));
            parser.registerParameter(new FlaggedOption("type").setShortFlag('t')
                                             .setLongFlag("type")
                                             .setDefault(new String[]{"act.*Document*", InvestigationArchetypes.PATIENT_INVESTIGATION})
                                             .setAllowMultipleDeclarations(true)
                                             .setHelp("The archetype short name. May contain wildcards."));
            parser.registerParameter(new FlaggedOption("failOnError")
                                             .setShortFlag('e')
                                             .setLongFlag("failOnError")
                                             .setDefault("false")
                                             .setStringParser(BooleanStringParser.getParser())
                                             .setHelp("Fail on error"));
            parser.registerParameter(new Switch("verbose").setShortFlag('v')
                                             .setLongFlag("verbose").setDefault("false").setHelp("Displays verbose info to the console."));
            parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                                             .setLongFlag("context")
                                             .setDefault(APPLICATION_CONTEXT)
                                             .setHelp("Application context path"));
        } catch (JSAPException exception) {
            throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.FailedToCreateParser, exception);
        }
        return parser;
    }

}
