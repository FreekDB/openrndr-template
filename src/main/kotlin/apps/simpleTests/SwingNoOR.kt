package apps.simpleTests

import java.awt.EventQueue
import java.awt.event.KeyEvent
import javax.swing.*
import kotlin.system.exitProcess

/**
 * Test using Swing from Kotlin. No OPENRNDR here yet.
 * I thought that maybe it's interesting for designing complex UIs
 * and then somehow communicating the UI application with the
 * OPENRNDR application.
 *
 * Passing lots of variables via OSC or some other method is not as
 * convenient as having direct access to variables and methods, as
 * one has when writing only one program instead of two. Is that solvable?
 */

class KotlinSwingSimpleEx(title: String) : JFrame() {
    init {
        createUI(title)
    }

    private fun createUI(title: String) {

        setTitle(title)

        val closeBtn = JButton("Close")

        closeBtn.addActionListener { exitProcess(0) }

        createLayout(closeBtn)
        createMenuBar()

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(300, 200)
        setLocationRelativeTo(null)
    }


    private fun createLayout(vararg arg: JComponent) {

        val gl = GroupLayout(contentPane)
        contentPane.layout = gl

        gl.autoCreateContainerGaps = true

        gl.setHorizontalGroup(
            gl.createSequentialGroup()
                .addComponent(arg[0])
        )

        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addComponent(arg[0])
        )

        pack()
    }

    private fun createMenuBar() {

        val menubar = JMenuBar()
        val icon = ImageIcon("src/main/resources/exit.png")

        val file = JMenu("File")
        file.mnemonic = KeyEvent.VK_F

        val eMenuItem = JMenuItem("Exit", icon)
        eMenuItem.mnemonic = KeyEvent.VK_E
        eMenuItem.toolTipText = "Exit application"
        eMenuItem.addActionListener { exitProcess(0) }

        file.add(eMenuItem)
        menubar.add(file)

        jMenuBar = menubar
    }
}

private fun createAndShowGUI() {
    val frame = KotlinSwingSimpleEx("Simple")

    frame.isVisible = true
}

fun main(args: Array<String>) {
    EventQueue.invokeLater(::createAndShowGUI)
}
