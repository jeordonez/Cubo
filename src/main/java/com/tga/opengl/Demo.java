package com.tga.opengl;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.nio.IntBuffer;
import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Demo {

    // The window handle
    private long window;
    int VAO, VBO, VBO2, VBO3, EBO;
    float angle = 0.0f;

    ShaderProgram shaderProgram;                                                                             
        Matrix4f projection = new Matrix4f();
        Matrix4f view = new Matrix4f(); // identity() not necesary because Matrix4f() generated a identitiy matrix
        Matrix4f model = new Matrix4f(); 
        
        int textureID;
        
        String fragmentShaderSource = "#version 330 core\n"
        + "out vec4 fragColor;\n"
        + "in vec3 fragPos;\n"
        + "in vec3 Normal;\n"
        + "in vec2 UV;\n"
        + "uniform vec3 lightPos;\n"
        + "uniform vec3 viewPos;"
        + "uniform vec3 lightColor;\n"
//        + "uniform vec3 objectColor;\n"
        + "uniform sampler2D diffuseTex;\n"
        + "const float ambientStrength = 0.3;\n"
        + "void main() {\n"
        + "// ambient\n"
        + " vec3 ambient = ambientStrength * lightColor;\n"
                
        + " // diffuse\n"
        + "vec3 norm = normalize(Normal);\n"
        + "vec3 lightDir = normalize(lightPos - fragPos);\n"
        + "float diff = max(dot(norm, lightDir), 0.0);\n"
        + "vec3 diffuse = diff * lightColor;\n"
        
        + "//Specular\n" 
        + "float specularStrength = 0.5;\n"
        + "vec3 viewDir = normalize(viewPos - fragPos);\n"
        + "vec3 reflectDir = reflect(-lightDir, norm);\n"
        + "float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);\n"
        + "vec3 specular = specularStrength * spec * lightColor;\n"
        
        //+ "vec3 result = (ambient + diffuse + specular) * vec3(1.0, 1.0, 1.0);\n"               
        //+ "vec3 result = (ambient + diffuse) * texture(diffuseTex, UV).rgb;\n"
        + "vec3 result = (ambient + diffuse + specular) * texture(diffuseTex, UV).rgb;\n"
        + "fragColor = vec4(result, 1.0);\n"
        + "}";
        /*
      
        + "float specularStrength = 0.5;"        
        + "vec3 viewDir = normalize(viewPos - fragPos);"
        + "vec3 reflectDir = reflect(-lightDir, norm);"
        + "float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32);"
        + "vec3 specular = specularStrength * spec * lightColor;"
//        + "vec3 result = (ambient + diffuse) * objectColor;\n"
        //+ "vec3 result = (ambient + diffuse + specular) * texture(diffuseTex, UV).rgb;\n"
          + "vec3 result = (diffuse + specular) * texture(diffuseTex, UV).rgb;\n"
        //+ "vec3 result = (ambient + diffuse) * texture(diffuseTex, UV).rgb;\n"
//        + "vec3 result = (ambient + diffuse + specular) * (objectColor + texture(diffuseTex, UV).rgb);\n"
//        + "fragColor = vec4(result, 1.0);\n"
        + "fragColor = vec4(result, 1.0);\n"
        //+ "if(texture(diffuseTex, UV).a < 1.0) discard;\n"
        +        " fragColor = vec4(vec3(diff), 1.0);\n"
        + "}";*/

        String vertexShaderSource = "#version 330 core\n"
        + "out vec3 fragPos;"                  
        + "layout (location = 0) in vec3 aPos;\n"
        + "layout (location = 1) in vec3 aNormal;\n"
        + "layout (location = 2) in vec2 aTexCoord;\n"
        + "uniform mat4 projection;\n"
        + "uniform mat4 view;\n"
        + "uniform mat4 model;\n"
        + "out vec3 Normal;\n"
        + "out vec2 UV;\n"
        + "void main()\n"
        + "{\n"
        + " gl_Position = projection * view * model * vec4(aPos, 1.0);\n"
        + " //Normal = aNormal;\n"
        + " UV = aTexCoord;\n"
        + "fragPos = vec3(model * vec4(aPos, 1.0));"
        + " mat3 normalMatrix = transpose(inverse(mat3(model)));\n"
        + " Normal = normalMatrix * aNormal;\n"
        + "}";

    public void run() throws Exception {
        init();
        
        float[] positions = new float[]{
	//Cara z = 1
	-1.0f,	-1.0f,	 1.0f, //0
	 1.0f,	-1.0f,	 1.0f, //1
	-1.0f,	 1.0f,	 1.0f, //2
	 1.0f,	 1.0f,	 1.0f, //3

	//Cara z = -1		   
	-1.0f,	-1.0f,	-1.0f, //4
	 1.0f,	-1.0f,	-1.0f, //5
	-1.0f,	 1.0f,	-1.0f, //6
	 1.0f,	 1.0f,	-1.0f, //7

	//Cara x = 1		   
	1.0f,	-1.0f,	-1.0f, //8
	1.0f,	-1.0f,	 1.0f, //9
	1.0f,	 1.0f,	-1.0f, //10
	1.0f,	 1.0f,	 1.0f, //11

	//Cara x = -1		   
	-1.0f,	-1.0f,	-1.0f, //12
	-1.0f,	-1.0f,	 1.0f, //13
	-1.0f,	 1.0f,	-1.0f, //14
	-1.0f,	 1.0f,	 1.0f, //15

	//Cara y = 1		   
	-1.0f,	 1.0f,	-1.0f, //16
	-1.0f,	 1.0f,	 1.0f, //17
	 1.0f,	 1.0f,	-1.0f, //18
	 1.0f,	 1.0f,	 1.0f, //19

	//Cara y = -1		   
	-1.0f,	-1.0f,	-1.0f, //20
	-1.0f,	-1.0f,	 1.0f, //21
	 1.0f,	-1.0f,	-1.0f, //22
	 1.0f,	-1.0f,	 1.0f  //23
        };


        float[] normals = new float[]{
	//Cara z = 1
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 
	0.0f,	0.0f,	 1.0f, 

	//Cara z = -1		   
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 
	0.0f,	0.0f,	-1.0f, 

	//Cara x = 1		   
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 
	1.0f,	0.0f,	 0.0f, 

	//Cara x = -1		   
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 
	-1.0f,	0.0f,	 0.0f, 

	//Cara y = 1		   
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 
	0.0f,	1.0f,	0.0f, 

	//Cara y = -1		   
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f, 
	0.0f,	-1.0f,	0.0f 
        };
        float[] texCoords = new float[]{
	//Cara z = 1
	 0.0f, 0.0f,
	 1.0f, 0.0f,
	 0.0f, 1.0f,
	 1.0f, 1.0f,

	//Cara z = -1
	0.0f, 1.0f, 
	1.0f, 1.0f, 
	0.0f, 0.0f, 
	1.0f, 0.0f, 

	//Cara x = 1	
	0.0f,	1.0f,
	1.0f,	1.0f,
	0.0f,	0.0f,
	1.0f,	0.0f,

	//Cara x = -1
	0.0f,	0.0f,
	1.0f,	0.0f,
	0.0f,	1.0f,
	1.0f,	1.0f,

	//Cara y = 1	
	0.0f, 1.0f,
	0.0f, 0.0f,
	1.0f, 1.0f,
	1.0f, 0.0f,

	//Cara y = -1
	0.0f, 0.0f,
	0.0f, 1.0f,
	1.0f, 0.0f,
	1.0f, 1.0f
        };
                //Indexado
        int [] indices = new int[] {
	//Cara z = 1
	0,1,2,			1,3,2,
	//Cara z = -1
	4,6,5,			5,6,7,
	//Cara x = 1
	8,10,9,			9,10,11,
	//Cara x = -1
	12,13,14,		13,15,14,
	//Cara y = 1
	16,17,18,		17,19,18,
	//Cara y = -1
	20,22,21,		21,22,23
        };

        FloatBuffer positionBuffer = null;
        FloatBuffer normalBuffer = null;
        FloatBuffer texCoordBuffer = null;
        IntBuffer indicesBuffer = null;
        try {
        positionBuffer = MemoryUtil.memAllocFloat(positions.length);
        positionBuffer.put(positions).flip();
        
        normalBuffer = MemoryUtil.memAllocFloat(normals.length);
        normalBuffer.put(normals).flip();
        
        texCoordBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texCoordBuffer.put(texCoords).flip();
        
        indicesBuffer = MemoryUtil.memAllocInt(indices.length);
        indicesBuffer.put(indices).flip();
        
        
        VAO = glGenVertexArrays(); // Create VertexArrayObject
        VBO = glGenBuffers(); // Create VertexBufferObject
        VBO2 = glGenBuffers();
        VBO3 = glGenBuffers();
        EBO = glGenBuffers();
        
        glBindVertexArray(VAO); // Bind current VAO
        
        //position attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(0); // Active attribute 0 on VAO
        
        //normal attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO2); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(1); // Active attribute 0 on VAO
        
        //textCoord attribute
        glBindBuffer(GL_ARRAY_BUFFER, VBO3); // Bind Vertex VAO
        glBufferData(GL_ARRAY_BUFFER, texCoordBuffer, GL_STATIC_DRAW); // Assign buffer to VBO
        glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0); // Definition of position attribute in buffer
        glEnableVertexAttribArray(2); // Active attribute 0 on VAO
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
        
        // Unbind VBO (0 is equal to null VBO)
        glBindBuffer(GL_ARRAY_BUFFER, 0); 
        glBindVertexArray(0); // Unbind VAO (0 is equal to null VAO)
        
        } finally {
            if (positionBuffer != null) {
                MemoryUtil.memFree(positionBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(normalBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(texCoordBuffer); // Destroy auxiliar buffer.
            }
            if (positionBuffer != null) {
                MemoryUtil.memFree(indicesBuffer); // Destroy auxiliar buffer.
            }
        }
        shaderProgram = new ShaderProgram();
	shaderProgram.createVertexShader(vertexShaderSource);
	shaderProgram.createFragmentShader(fragmentShaderSource);
	shaderProgram.link();
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("view");
        shaderProgram.createUniform("model");
        
        shaderProgram.createUniform("lightColor");
        shaderProgram.createUniform("lightPos");
//        shaderProgram.createUniform("objectColor");

        projection.perspective( (float) Math.toRadians(60.0f), 600.0f/600.0f, 0.001f, 1000.0f);
        view.setTranslation(new Vector3f(0.0f, 0.0f, -4.0f));             
        model.identity();//.translate(new Vector3f(0.0f, 1.0f, 0.0f)).rotate(angle, new Vector3f(1.0f, 0.0f, 0.0f));

        
            InputStream in = new FileInputStream("E:\\Master\\TAG\\OpenGLTemplate\\Cubo\\src\\main\\java\\com\\tga\\opengl\\metal.png");
//          InputStream in = new FileInputStream(classLoader.getResource("Mario.png").getFile());
            PNGDecoder decoder = new PNGDecoder(in);
            System.out.println("width=" + decoder.getWidth());
            System.out.println("height=" + decoder.getHeight());
            ByteBuffer buf = ByteBuffer.allocateDirect(4*decoder.getWidth()*decoder.getHeight());
            decoder.decode(buf, decoder.getWidth()*4, Format.RGBA);
            buf.flip();
            textureID = glGenTextures();
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID); // all upcoming GL_TEXTURE_2D operations now have effect on this texture object
            // set the texture wrapping parameters
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.getWidth(),
            decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glGenerateMipmap(GL_TEXTURE_2D);  
        


            
        
        
        loop();

        // Libera las devoluciones de llamada de la ventana y destruye la ventana
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() throws Exception {
        
        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        // Configure GLFW
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Create the window
        window = glfwCreateWindow(600, 600, "TAG", NULL, NULL);
        if (window == NULL) {
            glfwTerminate();
            throw new RuntimeException("Failed to create the GLFW window");
        }
        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            if (key == GLFW_KEY_Q) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.z += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.z);
            }
            if (key == GLFW_KEY_E) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.z -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.z);
            }
            if (key == GLFW_KEY_A) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.x -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.x);
            }
            if (key == GLFW_KEY_D) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.x += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.x);
            }
            if (key == GLFW_KEY_S) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.y -= 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.y);
            }
            if (key == GLFW_KEY_W) {
                Vector3f translation = new Vector3f();
                view.getTranslation(translation);
                translation.y += 0.1f;
                view = view.setTranslation(translation);
                System.out.println(translation.y);
            }
        });

        // Obtener la pila de hilos y empujar un nuevo marco
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Obtener el tamaño de ventana pasado a glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.
        GL.createCapabilities();
    }

    private void loop() throws Exception {
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        glEnable(GL_DEPTH_TEST);
        //glEnable(GL_BLEND);
        
        while (!glfwWindowShouldClose(window)) {

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
            glClearColor(0.2f, 0.3f, 0.3f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            shaderProgram.setUniform("projection", projection);
            shaderProgram.setUniform("view", view);
            
            shaderProgram.setUniform("lightColor", 1.0f,1.0f,1.0f);
            shaderProgram.setUniform("lightPos", 1.2f, 2.0f, 2.0f);
//            shaderProgram.setUniform("objectColor", 0.0f,0.0f,0.0f);
            
            //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            // render
            // ------
            
            //Textura     
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textureID);
            
     
            glBindVertexArray(VAO);
//          Rotacion acumulada cubo
            //angle += glfwGetTime();
            angle += 1;
            model.identity().translate(new Vector3f(0.0f, 0.0f, 0.0f)).rotate((float)Math.toRadians(angle), new Vector3f(0.0f, 1.0f, 0.0f));
            
            shaderProgram.setUniform("model", model);
            shaderProgram.bind(); 
            //glDrawArrays(GL_TRIANGLES, 0, 3);
            glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0); // 12*3 los índices comienzan en 0 -> 12 triángulos -> 6 cuadrados
            // glBindVertexArray(0); // no need to unbind it every time
            glfwSwapBuffers(window); // swap the color buffers
        }
    }

    public static void main(String[] args) throws Exception {
        new Demo().run();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    

}
