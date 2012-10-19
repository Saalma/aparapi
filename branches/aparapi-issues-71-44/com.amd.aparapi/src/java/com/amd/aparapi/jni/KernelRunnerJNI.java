package com.amd.aparapi.jni;

import java.util.List;

import com.amd.aparapi.Kernel;
import com.amd.aparapi.Range;
import com.amd.aparapi.device.OpenCLDevice;
import com.amd.aparapi.jni.OpenCLJNI.UsedByJNICode;
import com.amd.aparapi.util.Annotations;
import com.amd.aparapi.util.ProfileInfo;

/**
 * This class is intended to be used as a 'proxy' or 'facade' object for Java code to interact with JNI
 */
public class KernelRunnerJNI {

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>boolean</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_BOOLEAN = 1 << 0;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>byte</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_BYTE = 1 << 1;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>float</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_FLOAT = 1 << 2;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>int</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_INT = 1 << 3;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>double</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_DOUBLE = 1 << 4;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>long</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_LONG = 1 << 5;

   /**
    * TODO:
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_SHORT = 1 << 6;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents an array.<br/>
    * So <code>ARG_ARRAY|ARG_INT</code> tells us this arg is an array of <code>int</code>.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_ARRAY = 1 << 7;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a primitive (non array).<br/>
    * So <code>ARG_PRIMITIVE|ARG_INT</code> tells us this arg is a primitive <code>int</code>.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_PRIMITIVE = 1 << 8;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> is read by the Kernel (note from the Kernel's point of view).<br/>
    * So <code>ARG_ARRAY|ARG_INT|ARG_READ</code> tells us this arg is an array of int's that are read by the kernel.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_READ = 1 << 9;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> is mutated by the Kernel (note from the Kernel's point of view).<br/>
    * So <code>ARG_ARRAY|ARG_INT|ARG_WRITE</code> tells us this arg is an array of int's that we expect the kernel to mutate.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_WRITE = 1 << 10;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> resides in local memory in the generated OpenCL code.<br/>
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.annotations.Experimental
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @Annotations.Experimental
   @UsedByJNICode
   public static final int ARG_LOCAL = 1 << 11;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> resides in global memory in the generated OpenCL code.<br/>
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.annotations.Experimental
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @Annotations.Experimental
   @UsedByJNICode
   public static final int ARG_GLOBAL = 1 << 12;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> resides in constant memory in the generated OpenCL code.<br/>
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.annotations.Experimental
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @Annotations.Experimental
   @UsedByJNICode
   public static final int ARG_CONSTANT = 1 << 13;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> has it's length reference, in which case a synthetic arg is passed (name mangled) to the OpenCL kernel.<br/>
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_ARRAYLENGTH = 1 << 14;

   /**
    * TODO:
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_APARAPI_BUF = 1 << 15;

   /**
    * This 'bit' indicates that the arg has been explicitly marked for reading
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_EXPLICIT = 1 << 16;

   /**
    * This 'bit' indicates that the arg has been explicitly marked for writing
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_EXPLICIT_WRITE = 1 << 17;

   /**
    * TODO:
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_OBJ_ARRAY_STRUCT = 1 << 18;

   /**
    * TODO:
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   // @UsedByJNICode public static final int ARG_APARAPI_BUF_HAS_ARRAY = 1 << 19;

   /**
    * TODO:
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   // @UsedByJNICode public static final int ARG_APARAPI_BUF_IS_DIRECT = 1 << 20;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>char</code> type (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author rlamothe
    */
   @UsedByJNICode
   public static final int ARG_CHAR = 1 << 21;

   /**
    * This 'bit' indicates that a particular <code>KernelArg</code> represents a <code>static</code> field (array or primitive).
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.KernelRunner.KernelArg
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int ARG_STATIC = 1 << 22;

   public static final String CL_KHR_FP64 = "cl_khr_fp64";

   public static final String CL_KHR_SELECT_FPROUNDING_MODE = "cl_khr_select_fprounding_mode";

   public static final String CL_KHR_GLOBAL_INT32_BASE_ATOMICS = "cl_khr_global_int32_base_atomics";

   public static final String CL_KHR_GLOBAL_INT32_EXTENDED_ATOMICS = "cl_khr_global_int32_extended_atomics";

   public static final String CL_KHR_LOCAL_INT32_BASE_ATOMICS = "cl_khr_local_int32_base_atomics";

   public static final String CL_KHR_LOCAL_INT32_EXTENDED_ATOMICS = "cl_khr_local_int32_extended_atomics";

   public static final String CL_KHR_INT64_BASE_ATOMICS = "cl_khr_int64_base_atomics";

   public static final String CL_KHR_INT64_EXTENDED_ATOMICS = "cl_khr_int64_extended_atomics";

   public static final String CL_KHR_3D_IMAGE_WRITES = "cl_khr_3d_image_writes";

   public static final String CL_KHR_BYTE_ADDRESSABLE_SUPPORT = "cl_khr_byte_addressable_store";

   public static final String CL_KHR_FP16 = "cl_khr_fp16";

   public static final String CL_KHR_GL_SHARING = "cl_khr_gl_sharing";

   /**
    * This 'bit' indicates that we wish to enable profiling from the JNI code.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * 
    * @author gfrost
    */
   //@UsedByJNICode public static final int JNI_FLAG_ENABLE_PROFILING = 1 << 0;

   /**
    * This 'bit' indicates that we wish to store profiling information in a CSV file from JNI code.
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * 
    * @author gfrost
    */
   // @UsedByJNICode public static final int JNI_FLAG_ENABLE_PROFILING_CSV = 1 << 1;

   /**
    * This 'bit' indicates that we want to execute on the GPU.
    * 
    * 
    * Be careful changing final constants starting with JNI.<br/>
    * 
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * 
    * @author gfrost
    */
   @UsedByJNICode
   public static final int JNI_FLAG_USE_GPU = 1 << 2;

   /**
    * This 'bit' indicates that we wish to enable verbose JNI layer messages to stderr.<br/>
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.annotations.Experimental
    * 
    * @author gfrost
    */
   // @UsedByJNICode public static final int JNI_FLAG_ENABLE_VERBOSE_JNI = 1 << 3;

   /**
    * This 'bit' indicates that we wish to enable OpenCL resource tracking by JNI layer to be written to stderr.<br/>
    * 
    * @see com.amd.aparapi.annotations.UsedByJNICode
    * @see com.amd.aparapi.annotations.Experimental
    * 
    * @author gfrost
    */
   //  @UsedByJNICode @Annotations.Experimental public static final int JNI_FLAG_ENABLE_VERBOSE_JNI_OPENCL_RESOURCE_TRACKING = 1 << 4;

   /**
    * TODO:
    * 
    * synchronized to avoid race in clGetPlatformIDs() in OpenCL lib problem should fixed in some future OpenCL version
    * 
    * @param _kernel
    * @param _flags
    * @param numProcessors
    * @param maxJTPLocalSize
    * @return
    */
   @Annotations.DocMe
   public native static synchronized long initJNI(Kernel _kernel, OpenCLDevice device, int _flags);

   public native static int getJNI(long _jniContextHandle, Object _array);

   public native static long buildProgramJNI(long _jniContextHandle, String _source);

   public native static int setArgsJNI(long _jniContextHandle, KernelArgJNI[] _args, int argc);

   public native static int runKernelJNI(long _jniContextHandle, Range _range, boolean _needSync, int _passes);

   public native static int disposeJNI(long _jniContextHandle);

   public native static String getExtensionsJNI(long _jniContextHandle);

   // @Deprecated public native static int getMaxWorkGroupSizeJNI(long _jniContextHandle);

   // @Deprecated public native static int getMaxWorkItemSizeJNI(long _jniContextHandle, int _index);

   // @Deprecated public native static int getMaxComputeUnitsJNI(long _jniContextHandle);

   // @Deprecated public native static int getMaxWorkItemDimensionsJNI(long _jniContextHandle);

   public native static synchronized List<ProfileInfo> getProfileInfoJNI(long _jniContextHandle);
}
