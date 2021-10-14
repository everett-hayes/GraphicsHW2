import org.w3c.dom.HTMLCanvasElement
import org.khronos.webgl.WebGLRenderingContext as GL //# GL# we need this for the constants declared ˙HUN˙ a constansok miatt kell
import kotlin.js.Date
import vision.gears.webglmath.UniformProvider
import vision.gears.webglmath.Vec1
import vision.gears.webglmath.Vec2
import vision.gears.webglmath.Vec3
import vision.gears.webglmath.*
import kotlin.math.*

class Scene (
  val gl : WebGL2RenderingContext)
  //TODO: derive from UniformProvider
  {

  // val time by Vec1()

  // init {
  //   addComponentsAndGatherUniforms(*Program.all)
  // }

  val vsTrafo = Shader(gl, GL.VERTEX_SHADER, "trafo-vs.glsl")
  val fsSolid = Shader(gl, GL.FRAGMENT_SHADER, "solid-fs.glsl")
  val solidProgram = Program(gl, vsTrafo, fsSolid)
  val redMaterial = Material(solidProgram)
  val cyanMaterial = Material(solidProgram)
  val highlightMaterial = Material(solidProgram)

  //TODO: create various materials with different solidColor settings
  init {
    redMaterial["solidColor"]?.set(1.0f, 0.1f, 0.0f)
    cyanMaterial["solidColor"]?.set(0.0f, 1.0f, 1.0f)
    highlightMaterial["solidColor"]?.set(0.0f, 0.5f, 0.5f)
  }

  val triangleGeometry = TriangleGeometry(gl)

  val triangleMesh = Mesh(redMaterial, triangleGeometry);
  val otherTriangleMesh = Mesh(cyanMaterial, triangleGeometry);
  val meshes = ArrayList<Mesh>()
  var selectedIndex : Int? = null;
  val gameObjects = ArrayList<GameObject>()

  init {
    meshes += triangleMesh;
    meshes += otherTriangleMesh;

    for (mesh in meshes) {

      val avatar = object : GameObject(triangleMesh) {
        override fun move(
          dt : Float,
          t : Float,
          keysPressed : Set<String>,
          gameObjects : List<GameObject>
        ) : Boolean {

          if (keysPressed.contains("ArrowUp")) {
            position.y += .01f
          }
          
          if (keysPressed.contains("ArrowDown")) {
            position.y -= .01f
          }

          if (keysPressed.contains("ArrowLeft")) {
            position.x -= .01f
          }

          if (keysPressed.contains("ArrowRight")) {
            position.x += .01f
          }

          if (keysPressed.contains("a")) {
            roll += .015f
          }

          if (keysPressed.contains("d")) {
            roll -= .015f
          }

          return true
        }
      }

      gameObjects += avatar;
    }
  } 

  val camera = OrthoCamera(*Program.all).apply {
    position.set(1f, 1f)
    updateViewProjMatrix()
  }

  fun resize(canvas : HTMLCanvasElement) {
    gl.viewport(0, 0, canvas.width, canvas.height)
    camera.setAspectRatio(canvas.width.toFloat() / canvas.height.toFloat())
  }

  val timeAtFirstFrame = Date().getTime()
  var timeAtLastFrame =  timeAtFirstFrame

  var spacePressed : Boolean = false;
  var nPressed : Boolean = false;

  fun findNextSelected() {
    // if there exist some gameobjects
    if (gameObjects.size > 0) {
      // if we haven't picked one yet
      if (selectedIndex == null) {
        // pick a random one
        selectedIndex = (0..(gameObjects.size - 1)).random()
      } else {
        // pick a new one that is not the previous
        if (gameObjects.size > 1) {
          var temp = (0..(gameObjects.size - 1)).random();

          while (temp == selectedIndex) {
            temp = (0..(gameObjects.size - 1)).random();
          }

          selectedIndex = temp;
        }
      }
    }
  }

  fun addNewObject()  {

    console.log("adding a new object!!");

    val newMeshIndex = (0..(meshes.size - 1)).random();

    console.log("the new index is " + newMeshIndex);

    val avatar = object : GameObject(meshes[newMeshIndex]) {
      override fun move(
        dt : Float,
        t : Float,
        keysPressed : Set<String>,
        gameObjects : List<GameObject>
      ) : Boolean {

        if (keysPressed.contains("ArrowUp")) {
          position.y += .01f
        }
        
        if (keysPressed.contains("ArrowDown")) {
          position.y -= .01f
        }

        if (keysPressed.contains("ArrowLeft")) {
          position.x -= .01f
        }

        if (keysPressed.contains("ArrowRight")) {
          position.x += .01f
        }

        if (keysPressed.contains("a")) {
          roll += .015f
        }

        if (keysPressed.contains("d")) {
          roll -= .015f
        }

        return true
      }
    }

    gameObjects += avatar
  }

  @Suppress("UNUSED_PARAMETER")
  fun update(keysPressed : Set<String>) {

    val timeAtThisFrame = Date().getTime() 
    val dt = (timeAtThisFrame - timeAtLastFrame).toFloat() / 1000.0f
    val t = (timeAtThisFrame - timeAtFirstFrame).toFloat() / 1000.0f
    //TODO: set property time (reflecting uniform scene.time) 
    timeAtLastFrame = timeAtThisFrame
    
    gl.clearColor(0.3f, 0.0f, 0.3f, 1.0f)//## red, green, blue, alpha in [0, 1]
    gl.clearDepth(1.0f)//## will be useful in 3D ˙HUN˙ 3D-ben lesz hasznos
    gl.clear(GL.COLOR_BUFFER_BIT or GL.DEPTH_BUFFER_BIT)//#or# bitwise OR of flags

    // if I see a space and I haven't seen space
    if (" " in keysPressed && !spacePressed) {
      spacePressed = true;
      findNextSelected();
    }

    if (spacePressed && !(" " in keysPressed)) {
      spacePressed = false;
    }

    // if I see an n and I haven't seen n
    if ("n" in keysPressed && !nPressed) {
      nPressed = true;
      addNewObject();
    }

    if (nPressed && !("n" in keysPressed)) {
      nPressed = false;
    }

    if ("Backspace" in keysPressed) {
      if (gameObjects.size > 0 && selectedIndex != null) {
        gameObjects.removeAt(selectedIndex!!)
        selectedIndex = null;
      }
    }

    // gameObjects[1].roll += dt

    for (i in gameObjects.indices) {
      if (i == selectedIndex) continue;
      gameObjects[i].update();
    }

    for (i in gameObjects.indices) {
      if (i == selectedIndex) continue;
      gameObjects[i].draw(camera)
    }

    if (selectedIndex != null) {
      gameObjects[selectedIndex!!].move(dt, t, keysPressed, gameObjects)
      gameObjects[selectedIndex!!].update()
      gameObjects[selectedIndex!!].using(highlightMaterial).draw(camera)
    }
  }
}
