package net.iowntheinter.cintershell.impl

import io.vertx.core.buffer.Buffer
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.ext.shell.command.CommandBuilder
import io.vertx.groovy.ext.shell.command.CommandProcess
import io.vertx.groovy.ext.shell.command.CommandRegistry
import io.vertx.groovy.ext.shell.session.Session

//docs: leave refrence (url or magnet link) to key:documentation
//have ? fetch the dox and display them

/**
 * Created by grant on 11/6/15.
 */
class commandOneShot {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    Vertx vertx
    Closure command
    Map validationHdlr;
    Map ansAr = [:];
    Closure finish
    Logger log
    String intro = "";

    commandOneShot(v, String name, String intr = "", Closure cmd, Map respHdl, Closure finishAction) {
        vertx = v as Vertx
        command = cmd  //this is a hack, need to figure out a clean way to get the inital key set
        finish = finishAction;
        intro = intr
        validationHdlr = respHdl;
        def builder = CommandBuilder.command(name)
        builder.processHandler(hdlr)
        // Register the command
        def registry = CommandRegistry.getShared(vertx)
        registry.registerCommand(builder.build(vertx))
        log = LoggerFactory.getLogger('net.iowntheinter.cintershell.impl.commandOneShot')
    }


    def hdlr = { CommandProcess pr ->
        def v = this.getVertx()
        def session = pr.session() as Session
        session.put('DiagCounter', 0)
        session.put('Args', pr.args())
        session.put('ansAr', ansAr)
        session.put('validationHdlr', validationHdlr)
        command([v:vertx,p:pr,v:vertx], finish)
    }

}
