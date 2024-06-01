import java.io.File
import kotlin.math.min

val docs = File("./")
  .listFiles()!!
  .filter { it.isDirectory() }

docs.forEach { dir ->
  val index = File(dir, "index.txt")
    .takeIf { it.exists() }
    ?.apply {
      val tree = readIndex(this)
      val files = dir.listFiles()!!
      setFileName(tree, files)
    }
}

fun setFileName(tree: Indexer.TreeNode, files: Array<File>, prefix: String = "") {
  tree.children.forEachIndexed { index, node ->
    val file = files.find { (if (it.isFile) it.nameWithoutExtension else it.name).run {
      replace(node.name, "").length == indexOf(node.name)
          && (indexOf(node.name) == 0 || this[indexOf(node.name) - 1] == '.')
    } }

    file?.apply {
      renameTo(File(parentFile,
        if (isFile) "$prefix${index + 1}.${node.name}.${extension}"
        else "$prefix${index + 1}.${node.name}"
      ))

      if (isDirectory) {
        setFileName(node, listFiles(), "$prefix${index + 1}.")
      }
    }
  }
}

class TreeNode(val name: String, val depth: Int){
  val children = mutableListOf<TreeNode>()

  fun findChild(name: String) = children.find { it.name == name }

  override fun toString(): String {
    val builder = StringBuilder()
    builder.append(" ".repeat(depth*4))
    builder.append(name)
    builder.append("\n")
    children.forEach{ builder.append(it.toString()) }

    return builder.toString()
  }
}

fun readIndex(index: File): TreeNode {
  if (!index.exists()) return TreeNode("root", 0)
  val lines = index.readLines().filter { it.isNotBlank() }

  val root = TreeNode("root", 0)
  parseLines(lines, root, 0)

  return root
}

fun parseLines(lines: List<String>, parent: TreeNode, padding: Int){
  lines.forEachIndexed { index, line ->
    if (!line.startsWith(" ".repeat(padding)) || line[padding] == ' ') return@forEachIndexed

    val name = line.substring(padding)
    val node = TreeNode(name, parent.depth + 1)

    val sub = lines.subList(min(index + 1, lines.size - 1), lines.size)
    val end = sub.indexOfFirst { it.indexOf(it.trimStart()) <= padding }.let { if (it == -1) sub.size else it }
    if (end > 0){
      val next = sub.first()
      parseLines(sub.subList(0, end), node, next.indexOf(next.trimStart()))
    }

    parent.children.add(node)
  }
}