
package com.tga.opengl;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VALIDATE_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import org.lwjgl.system.MemoryStack;

public class ShaderProgram {
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;
    public ShaderProgram() throws Exception {
    programId = glCreateProgram();
    if (programId == 0) { new Exception("ShaderProgram failed"); }
    uniforms = new HashMap<>();
    }
                public void createUniform(String name)
                   {
                       int v = glGetUniformLocation(programId, name);
                       if( v < 0 ) 
                       {

                       }
                       uniforms.put(name, v);
                   }
	
                public void setUniform(String name, Matrix4f value) {
                // Dump the matrix into a float buffer
                    try (MemoryStack stack = MemoryStack.stackPush()) {
                        FloatBuffer fb = stack.mallocFloat(16);
                        value.get(fb);
                        glUniformMatrix4fv(uniforms.get(name), false, fb);
                    }  
                }
                public void setUniform(String name, float v1, float v2, float v3) {
			glUniform3f(uniforms.get(name), v1, v2, v3);
		}
                
                public void setUniform(String name, int value) {
                    glUniform1i(uniforms.get(name), value);
                }
		
    public void createVertexShader(String sc) throws Exception {
        vertexShaderId = createShader(sc, GL_VERTEX_SHADER);
    }
    public void createFragmentShader(String sc) throws Exception {
        vertexShaderId = createShader(sc, GL_FRAGMENT_SHADER);
    }
    protected int createShader(String sc, int shaderType) throws Exception {
    int shaderId = glCreateShader(shaderType);
    if (shaderId == 0) {
    throw new Exception("Error creating shader. Type: " + shaderType);
    }
    glShaderSource(shaderId, sc);
    glCompileShader(shaderId);
    if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
    throw new Exception("Error compiling Shader code: " + glGetShaderInfoLog(shaderId, 1024));
    }
    glAttachShader(programId, shaderId);
    return shaderId;
    }
    public void link() throws Exception {
    glLinkProgram(programId);
    if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
    throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));
    }
    if (vertexShaderId != 0) { glDetachShader(programId, vertexShaderId); }
    if (fragmentShaderId != 0) { glDetachShader(programId, fragmentShaderId); }
    glValidateProgram(programId);
    if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0) {
    System.err.println("Warning validating Shader code: " + glGetProgramInfoLog(programId, 1024));
    }
    }
    public void bind() { glUseProgram(programId); }
    public void unbind() { glUseProgram(0); }
    public void cleanup() {
    unbind();
    if (programId != 0) { glDeleteProgram(programId); }
    }
}
