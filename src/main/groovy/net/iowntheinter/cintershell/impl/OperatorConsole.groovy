package net.iowntheinter.cintershell.impl

import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.shell.ShellServer
import io.vertx.ext.shell.ShellServerOptions
import io.vertx.ext.shell.ShellServerOptionsConverter
import io.vertx.ext.shell.ShellService
import io.vertx.ext.shell.ShellServiceOptions
import io.vertx.ext.shell.ShellServiceOptionsConverter
import io.vertx.ext.shell.command.CommandRegistry
import io.vertx.ext.shell.command.impl.CommandRegistryImpl
import io.vertx.ext.shell.term.SSHTermOptions
import io.vertx.ext.shell.term.impl.SSHServer
import io.vertx.ext.shell.term.impl.TelnetTermServer

/**
 * Created by grant on 10/28/15.
 */
class OperatorConsole {
    def vertx
    def logger = LoggerFactory.getLogger("OperatorConsole")
    def shadowmsg
    def config
    ShellServer server
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001B[0m";


    OperatorConsole(Vertx vx, JsonObject opts) {
        def sso = new ShellServerOptions()
        vertx = vx as Vertx
        config = vertx.getOrCreateContext().config()
        logger.info("attempting to start  service ")

        loader({
            sso.setWelcomeMessage(ANSI_RED + shadowmsg + ANSI_RESET)
           // service = ShellService.create(vx, sso)

            CommandRegistry register = CommandRegistry.getShared(vx); // Create appropriate command registry

            server = ShellServer.create(vertx, sso);
            server.registerTermServer(
                    new SSHServer(vertx,
                            new SSHTermOptions(opts.getJsonObject("sshOptions"))));
            server.registerCommandResolver(register);
            register.registerCommand(io.vertx.ext.shell.command.base.Help)
            try {
                server.listen({ result ->
                    if(!result.succeeded())
                        logger.error(result.cause())
                });

            } catch (e) {
                logger.error("could not start shell server: ${e}")
                e.printStackTrace()

            }
        })
    }
    void stop(){
        server.close()
    }
    void loader(cb) {
        try {
            def bfile = "_branding.txt"
            def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
            def bpth = classloader.findResource(bfile);
            logger.info("bfile ref: ${bpth}\n")

            shadowmsg = classloader.getResourceAsStream(bfile).getText()
          //  logger.info("${ANSI_RED + shadowmsg + ANSI_RESET}")
        } catch (nobranding) {
            logger.error("no branding detected, ${nobranding}")
            try {
                def bfile = "cintershell-banner.txt"
                def classloader = (URLClassLoader) (Thread.currentThread().getContextClassLoader())
                def bpth = classloader.findResource(bfile);
                logger.info("bfile ref: ${bpth}\n")

                shadowmsg = classloader.getResourceAsStream(bfile).getText()
                logger.info("${ANSI_RED + shadowmsg + ANSI_RESET}")
            } catch (e) {
                logger.error("could not get banner, ${e}")
                shadowmsg = "Welcome to cintershell \n"
            }
        }
        cb()
    }



}

