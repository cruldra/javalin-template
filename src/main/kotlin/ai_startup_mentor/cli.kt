package ai_startup_mentor

import picocli.CommandLine


@CommandLine.Command(
    name = "asm",
    description = ["AI创业大师API命令行工具"],
    subcommands = [
        Cli.Serve::class,
    ]
)
class Cli {

    @CommandLine.Command(name = "serve", description = ["作为远程服务运行"])
    class Serve : Runnable {
        override fun run() {
            createHttpServer()
        }
    }
}

fun main(vararg args: String) {
    val cli = CommandLine(Cli())
    cli.execute(*args)
}
