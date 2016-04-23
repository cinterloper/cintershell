package net.iowntheinter.cintershell.impl.cmds.example

import io.vertx.core.Vertx
import io.vertx.core.cli.CLI
import io.vertx.core.cli.CommandLine
import io.vertx.core.cli.Option
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.shell.session.Session
import io.vertx.ext.shell.command.CommandProcess


/**
 * Created by grant on 11/17/15.
 */
class TestOneShot {
    static String NAME = 'testoneshot'
    static String INTRO = new String(" this is a sample command \n\n")
    //you can actually rewrite the reactions through the process handle when questions is processed


    public static Closure COMMAND = { Map cmdctx, cb ->
        CommandProcess pr = cmdctx.p
        Session session = pr.session()
        ArrayList<String> args = session.get('Args')
        //walk all the validation handlers (defined below) and check their corespondant arguments
        //this should probably be a helper utility

        CLI cli = CLI.create(NAME)
        cli.setSummary(INTRO)
                .addOption(new Option()
                    .setLongName("test")
                    .setShortName('t')
                    .setDescription("itsa pizza!")
                    .setFlag(true))
                .addOption(new Option()
                    .setArgName("help")
                    .setShortName("h")
                    .setLongName("help").setHelp(true));

        StringBuilder builder = new StringBuilder();
        cli.usage(builder);
        CommandLine commandLine = cli.parse(args);

        commandLine.allArguments().each { it ->
            pr.write("arg: " + it)
        }
        if(commandLine.isAskingForHelp())
            pr.write(builder.toString())
        cb([v: cmdctx.v, p: cmdctx.p, d: commandLine.getOptionValue('test')])

    }






    public static Closure FINISH = { ctx ->
        def logger = LoggerFactory.getLogger("directoryConnection")
        logger.info("running finish closure with context ${ctx}")
        def v = ctx.v as Vertx
        def p = ctx.p as CommandProcess
        def d = ctx.d
        def eb = v.eventBus();
        eb.send('questions', d)
        println("result:${d}")
        p.end()
    }


}
