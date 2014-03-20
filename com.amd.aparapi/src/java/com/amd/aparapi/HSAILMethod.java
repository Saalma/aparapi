package com.amd.aparapi;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: gfrost
 * Date: 4/27/13
 * Time: 9:48 AM
 * To change this template use File | Settings | File Templates.
 */


    class HSAILStackFrame {
        public int stackOffset;
        HSAILStackFrame parent;
        private String nameSpace;
        private String uniqueName;
        int id=0;
        private int callPc;

    int stackIdx(Instruction _i){
        return(_i.getPreStackBase() + _i.getMethod().getCodeEntry().getMaxLocals()+ stackOffset);
    }
    String getUniqueName(){
        return(uniqueName);
    }
    String getUniqueLocation(int _pc){
        return(String.format("%s_%04d", getUniqueName(), _pc));
    }
    int incId(){
       if (parent == null){
           return(id++);
       }
       return(parent.incId());
    }

        HSAILStackFrame( HSAILStackFrame _parent, ClassModel.ClassModelMethod _calledMethod, int _callPc, int _stackOffset){

            parent = _parent;
            stackOffset = _stackOffset;
            callPc = _callPc;
            nameSpace=_calledMethod.getClassModel().getDotClassName()+"."+_calledMethod.getName()+_calledMethod.getDescriptor();
            synchronized (HSAILStackFrame.class){
                uniqueName = String.format("%04d", incId());
            }
        }



        public HSAILRenderer renderStack(HSAILRenderer rc) {
           if (parent != null){
               parent.renderStack(rc);
           }
           rc.pad(5).append(String.format("%04d -> %s", callPc, nameSpace)).nl();
           return(rc);
        }
    }



   abstract class InlineIntrinsicCall {

      boolean isStatic;
      private String mappedMethod; // i.e  java.lang.Math.sqrt(D)D

      String getMappedMethod() {
         return (mappedMethod);
      }

      public abstract void add(HSAILAssembler _assembler,  Instruction _from);


      InlineIntrinsicCall(String _mappedMethod, boolean _isStatic) {
         mappedMethod = _mappedMethod;
         isStatic = _isStatic;
        }

    }

class CallInfo{
    Instruction from;
    String name;
    String intrinsicLookupName;
    String dotClassName;
    String sig = null;
    CallInfo( Instruction _from) {
        from  = _from;
        if (from.isInterfaceMethodCall()){
            dotClassName = from.asInterfaceMethodCall().getConstantPoolInterfaceMethodEntry().getClassEntry().getDotClassName();
            name = from.asInterfaceMethodCall().getConstantPoolInterfaceMethodEntry().getName();
            sig = from.asInterfaceMethodCall().getConstantPoolInterfaceMethodEntry().getNameAndTypeEntry().getDescriptor();

            /** sig to specialize CharSequence to String  - big hack!**/
            if (dotClassName.equals("java.lang.CharSequence")){
                System.out.println("Specializing java.lang.CharSequence to java.lang.String!!!!! ");
                dotClassName = "java.lang.String";
            }

        }else{
            dotClassName = from.asMethodCall().getConstantPoolMethodEntry().getClassEntry().getDotClassName();
            name = from.asMethodCall().getConstantPoolMethodEntry().getName();

            sig = from.asMethodCall().getConstantPoolMethodEntry().getNameAndTypeEntry().getDescriptor();
        }
        intrinsicLookupName = dotClassName + "." + name + sig;
    }
}

class HSAILIntrinsics {
    public static Map<String, InlineIntrinsicCall> intrinsicMap = new HashMap<String, InlineIntrinsicCall>();

    public static void add(InlineIntrinsicCall _intrinsic) {
        intrinsicMap.put(_intrinsic.getMappedMethod(), _intrinsic);
    }
    static InlineIntrinsicCall getInlineIntrinsic(CallInfo _callInfo){
        return(intrinsicMap.get(_callInfo.intrinsicLookupName));
    }
    static {
        add(new InlineIntrinsicCall("java.lang.Math.sqrt(D)D", true){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                //   nsqrt_f64  $d${0}, $d${0};
                  _assembler.nsqrt(_from, _assembler.stackIdx(_from));
            }
        });
        add(new InlineIntrinsicCall( "java.lang.String.charAt(I)C", false){
            public void  add(HSAILAssembler _assembler,  Instruction _from){
                // ld_global_u64 $d${2}, [$d${0}+16];   // this string reference into $d${2}"
                // mov_b32 $s${3}, $s${1};              // copy index",
                // cvt_u64_s32 $d${3}, $s${3};          // convert array index to 64 bits",
                // mad_u64 $d${3}, $d${3}, 2, $d${2};   // get the char address",
                // ld_global_u16 $s${0}, [$d${3}+24];   // ld the char"

                // ld_global_u64 $d${2}, [$d${0}+16];   // this string reference into $d${2}"
                _assembler.ld_global_u64(_from, _assembler.stackIdx(_from) + 2, _assembler.stackIdx(_from), 16);

                // mov_b32 $s${3}, $s${1};              // copy index",
                _assembler.mov_s32(_from, _assembler.stackIdx(_from) + 3, _assembler.stackIdx(_from) + 1);

                // cvt_u64_s32 $d${3}, $s${3};          // convert array index to 64 bits",
                _assembler.cvt_u64_s32(_from, new StackReg_u64(_assembler.stackIdx(_from) + 3), new StackReg_s32(_assembler.stackIdx(_from) + 3));


                // mad_u64 $d${3}, $d${3}, 2, $d${2};   // get the char address",
                _assembler.add(new HSAILInstructionSet.mad(_assembler.currentFrame(), _from, new StackReg_ref(_assembler.stackIdx(_from)+3),new StackReg_ref(_assembler.stackIdx(_from)+3), new StackReg_ref(_assembler.stackIdx(_from)+2), 2));

                // ld_global_u16 $s${0}, [$d${3}+24];   // ld the char"
                _assembler.add(new HSAILInstructionSet.field_load(_assembler.currentFrame(), _from, new StackReg_u16(_assembler.stackIdx(_from)),  new StackReg_ref(_assembler.stackIdx(_from)+3), 24));

            }
        });
        add(new InlineIntrinsicCall("java.lang.Math.cos(D)D", true){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                _assembler.add(new HSAILInstructionSet.ncos(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from))));

            }
        });
        add(new InlineIntrinsicCall("java.lang.Math.sin(D)D", true ){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                _assembler.add(new HSAILInstructionSet.nsin(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from))));

            }
        });
        add(new InlineIntrinsicCall("java.lang.Math.hypot(DD)D", true ){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                //mul_f64 $d0, $d0, $d0;",
                //mul_f64 $d1, $d1, $d1;",
                //add_f64 $d0, $d0, $d1;",
                //nsqrt_f64  $d0, $d0;",
                _assembler.add(new HSAILInstructionSet.mul(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from)),  new StackReg_f64(_assembler.stackIdx(_from)),  new StackReg_f64(_assembler.stackIdx(_from))));
                _assembler.add(new HSAILInstructionSet.mul(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from)+1),  new StackReg_f64(_assembler.stackIdx(_from)+1),  new StackReg_f64(_assembler.stackIdx(_from)+1)));
                _assembler.add(new HSAILInstructionSet.add(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from)),  new StackReg_f64(_assembler.stackIdx(_from)),  new StackReg_f64(_assembler.stackIdx(_from)+1)));
                _assembler.add(new HSAILInstructionSet.nsqrt(_assembler.currentFrame(), _from,  new StackReg_f64(_assembler.stackIdx(_from))));

            }
        });
        add(new InlineIntrinsicCall("java.lang.Math.min(II)I", true ){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                // cmp_ge_b1_s32 $c1, $s0, $s1;
                // cmov_b32 $s0, $c1, $s1, $s0;
                _assembler.add(new HSAILInstructionSet.cmp_s32(_assembler.currentFrame(), _from, "ge", new StackReg_s32(_assembler.stackIdx(_from)),  new StackReg_s32(_assembler.stackIdx(_from)+1))) ;
                _assembler.add(new HSAILInstructionSet.cmov(_assembler.currentFrame(), _from,  new StackReg_s32(_assembler.stackIdx(_from)),  new StackReg_s32(_assembler.stackIdx(_from)+1),  new StackReg_s32(_assembler.stackIdx(_from))));


            }
        });
        add(new InlineIntrinsicCall("java.lang.Math.max(II)I", true ){
            public void add(HSAILAssembler _assembler,  Instruction _from){
                // cmp_le_b1_s32 $c1, $s0, $s1;
                // cmov_b32 $s0, $c1, $s1, $s0;
                _assembler.add(new HSAILInstructionSet.cmp_s32(_assembler.currentFrame(), _from, "le", new StackReg_s32(_assembler.stackIdx(_from)),  new StackReg_s32(_assembler.stackIdx(_from)+1))) ;
                _assembler.add(new HSAILInstructionSet.cmov(_assembler.currentFrame(), _from,  new StackReg_s32(_assembler.stackIdx(_from)),  new StackReg_s32(_assembler.stackIdx(_from)+1),  new StackReg_s32(_assembler.stackIdx(_from))));


            }
        });


    }


}

public class HSAILMethod {
   HSAILAssembler assembler;
    ClassModel.ClassModelMethod method;

    public HSAILRenderer render(HSAILRenderer r) {
        r.append("version 0:95: $full : $large").semicolon().nl();
        r.append("kernel &run").oparenth();
        boolean firstArg=true;
        int argc = 0;
        if (method.isNonStatic()) {
            r.nl().pad(3).kernarg(ref.ref, 0);
            firstArg = false;
            argc++;
        }

        for (TypeHelper.JavaMethodArg arg : method.argsAndReturnType.getArgs()) {
            if (!firstArg) {
                r.separator();
            }
            r.nl().pad(3).kernarg(arg.getJavaType().getPrimitiveType(), argc++);
            firstArg=false;
        }
        r.nl().pad(3).cparenth().obrace().nl();

        Instruction last = null; // we track the last bytecode instruction (not HSAIL) here so that we con emit branch labels and comments only once for each mapped instruction
        for (HSAILInstructionSet.HSAILInstruction i : assembler.getInstructions()) {
            if ((i  instanceof HSAILInstructionSet.ld_kernarg) || (i instanceof HSAILInstructionSet.workitemabsid)){

            }else if ( (last == null || last != i.from)) {
                if (i.from.isBranchTarget()) {
                    r.label(i.location).colon();
                }
                if (r.isShowingComments()) {
                    r.nl().pad(1).lineCommentStart().mark().append(i.location).relpad(2).space().i(i.from).nl();
                }
                last = i.from;
            }else{
                last = i.from;
            }
            r.pad(9);
            i.render(r);
            r.nl();
        }
        r.cbrace().semicolon().nl();
        r.commentStart();
        for (HSAILStackFrame hsailStackFrame:assembler.getFrameSet()){
            r.nl().append(hsailStackFrame.getUniqueName()).append("=").obrace().nl();
            hsailStackFrame.renderStack(r).cbrace().nl();
        }
        r.nl().commentEnd();
        return (r);
    }

    static synchronized HSAILMethod getHSAILMethod(ClassModel.ClassModelMethod _method) {
        HSAILMethod instance = new HSAILMethod(_method);
        return (instance);
    }

    private HSAILMethod(ClassModel.ClassModelMethod _method) {
        method = _method;

        assembler = new HSAILAssembler(method);


        if (UnsafeWrapper.addressSize() == 4) {
            throw new IllegalStateException("Object pointer size is 4, you need to use 64 bit JVM and set -XX:-UseCompressedOops!");
        }


        Instruction initial = method.getInstructions().iterator().next();
        int argOffset = 0;
        if (!method.isStatic()) {
            assembler.ld_kernarg_ref(initial, 0);
            // HSAILInstructionSet.ld_arg_ref( initial, 0); // if we need to support real calls.
            argOffset++;
        }
        TypeHelper.JavaMethodArg[] args = method.argsAndReturnType.getArgs();
        int argc = args.length;
        for (TypeHelper.JavaMethodArg arg : args) {
            if (arg.getJavaType().isArray() || arg.getJavaType().isObject()) {
                assembler.ld_kernarg_ref( initial, arg.getArgc() + argOffset);
                // assembler.ld_arg_ref( initial, arg.getArgc() + argOffset); // if we need to support real calls.
            } else if (arg.getJavaType().isInt()) {
                assembler.ld_kernarg_s32(initial, arg.getArgc() + argOffset);
                // assembler.ld_arg_s32( initial, arg.getArgc() + argOffset); // if we need to support real calls.
            } else if (arg.getJavaType().isFloat()) {
                assembler.ld_kernarg_f32(initial, arg.getArgc() + argOffset);
                // assembler.ld_arg_f32( initial, arg.getArgc() + argOffset); // if we need to support real calls.
            } else if (arg.getJavaType().isDouble()) {
                assembler.ld_kernarg_f64(initial, arg.getArgc() + argOffset);
                // assembler.ld_arg_f64( initial, arg.getArgc() + argOffset); // if we need to support real calls.
            } else if (arg.getJavaType().isLong()) {
                assembler.ld_kernarg_s64(initial, arg.getArgc() + argOffset);
                // assembler.ld_arg_s64( initial, arg.getArgc() + argOffset); // if we need to support real calls.
            }
        }
        assembler.workitemabsid_u32(initial, argc + argOffset); // we overwrite the last arg +1 with the gid
        assembler.add_s32(initial, argc + argOffset - 1, argc + argOffset - 1, argc + argOffset);
        assembler.addInstructions( method);
    }
}
