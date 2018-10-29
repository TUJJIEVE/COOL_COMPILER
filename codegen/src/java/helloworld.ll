; I am a comment in LLVM-IR. Feel free to remove me.
; ModuleID = helloworld.cl
source_filename = "helloworld.cl"
target datalayout = e-m:e-i64:64-f80:128-n8:16:32:64-S128
target triple = x86_64
;Structures of the classes
;The struct for Main
%class.Main = type { i32, i32, i32 }
;The struct for Hi
%class.Hi = type { i32, %class.Main* }
define dso_local void @_ZN4Main(%class.Main* %this){
entry:
  %this.addr = alloca %class.Main*, align null
  store %class.Main* %this , %class.Main** %this.addr	 ; store instruction if type Main
  %this1 = load %class.Main* , %class.Main** %this.addr 	;yields Main:val
  %0 = getelementptr inbounds %class.Main*, %class.Main** %this1, i32 0,i32 0
  %2 = alloca i32, align 4
  store i32 12 , i32* %2	;storing value
  %1 = load i32 , i32* %2 	;yields Int:val
  store i32 %1 , i32* %0	 ; store instruction if type Int
  %3 = getelementptr inbounds %class.Main*, %class.Main** %this1, i32 0,i32 1
  %5 = alloca i32, align 4
  store i32 13 , i32* %5	;storing value
  %4 = load i32 , i32* %5 	;yields Int:val
  store i32 %4 , i32* %3	 ; store instruction if type Int
  %6 = getelementptr inbounds %class.Main*, %class.Main** %this1, i32 0,i32 2
  %8 = add i32 null , null
  %7 = load i32 , i32* %8 	;yields Int:val
  store i32 %7 , i32* %6	 ; store instruction if type Int
  ret void
}
define dso_local void @_ZN2Hi(%class.Hi* %this){
entry0:
  %this.addr = alloca %class.Hi*, align null
  store %class.Hi* %this , %class.Hi** %this.addr	 ; store instruction if type Hi
  %this1 = load %class.Hi* , %class.Hi** %this.addr 	;yields Hi:val
  %9 = bitcast %class.Hi* %this1 to %class.Main*
  call void @_ZN4Main (%class.Main* %9)
  %10 = getelementptr inbounds %class.Hi*, %class.Hi** %this1, i32 0,i32 0
  %12 = alloca i32, align 4
  store i32 14 , i32* %12	;storing value
  %11 = load i32 , i32* %12 	;yields Int:val
  store i32 %11 , i32* %10	 ; store instruction if type Int
  %13 = getelementptr inbounds %class.Hi*, %class.Hi** %this1, i32 0,i32 1
  ret void
}
;Class Main method hello
define dso_local i32 @_ZN4Main5hello (%class.Main* %this, i32 %x) {
entry1:
  x.addr = alloca i32, align 4
  store i32 %x , i32* %x.addr	 ; store instruction if type Int
  %1 = alloca i32, align 4
  store i32 2 , i32* %1	;storing value
  %0 = add i32 %1 , null
  %2 = bitcast null* %0 to i32
  ret i32 %2
;Class Main method main
define dso_local void @_ZN4Main4main (%class.Main* %this) {
entry2:
  %0 = bitcast null* null to void
  ret void %0
