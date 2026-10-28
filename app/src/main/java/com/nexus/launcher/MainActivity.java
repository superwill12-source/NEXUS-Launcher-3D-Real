package com.nexus.launcher;

import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.nio.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.Matrix;

public class MainActivity extends Activity {
    private NexusOverlay overlay;
    private GlobeSurface globe;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(2,7,20));
        getWindow().setNavigationBarColor(Color.rgb(2,7,20));
        FrameLayout root = new FrameLayout(this);
        globe = new GlobeSurface(this);
        root.addView(globe, new FrameLayout.LayoutParams(-1,-1));
        overlay = new NexusOverlay(this);
        root.addView(overlay, new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
    }
    @Override protected void onResume(){super.onResume();if(globe!=null)globe.onResume();}
    @Override protected void onPause(){if(globe!=null)globe.onPause();super.onPause();}
    @Override public void onBackPressed(){if(overlay.page!=0){overlay.page=0;overlay.invalidate();}else super.onBackPressed();}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    void openApp(String label) {
        if(label.equals("Config.")||label.equals("Configurações")){try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}catch(Exception ignored){}return;}
        if(label.equals("Telefone")){try{startActivity(new Intent(Intent.ACTION_DIAL));}catch(Exception ignored){}return;}
        if(label.equals("Câmera")){try{startActivity(new Intent("android.media.action.IMAGE_CAPTURE"));}catch(Exception ignored){}return;}
        if(label.equals("Web")){searchWeb("https://www.google.com");return;}
        PackageManager pm=getPackageManager();
        ArrayList<ApplicationInfo> list=new ArrayList<>();
        for(ApplicationInfo a:pm.getInstalledApplications(0)) if(pm.getLaunchIntentForPackage(a.packageName)!=null)list.add(a);
        for(ApplicationInfo a:list){String n=a.loadLabel(pm).toString();if(n.equalsIgnoreCase(label)||n.toLowerCase().contains(label.toLowerCase())){try{startActivity(pm.getLaunchIntentForPackage(a.packageName));return;}catch(Exception ignored){}}}
        toast("Aplicativo não encontrado: "+label);
    }
    void searchWeb(String url){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(Exception e){toast("Navegador indisponível");}}
    void searchDialog(){
        EditText input=new EditText(this);input.setSingleLine(true);input.setHint("App, pessoa, lugar ou pesquisar na web");
        new AlertDialog.Builder(this).setTitle("NEXUS • Pesquisa universal").setView(input)
        .setPositiveButton("Pesquisar",(d,w)->{
            String q=input.getText().toString().trim();if(q.isEmpty())return;
            PackageManager pm=getPackageManager();
            for(ApplicationInfo a:pm.getInstalledApplications(0)){String n=a.loadLabel(pm).toString();if(n.toLowerCase().contains(q.toLowerCase())&&pm.getLaunchIntentForPackage(a.packageName)!=null){openApp(n);return;}}
            searchWeb("https://www.google.com/search?q="+Uri.encode(q));
        }).setNegativeButton("Cancelar",null).show();
        input.requestFocus();input.postDelayed(()->((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(input,InputMethodManager.SHOW_IMPLICIT),250);
    }

    class GlobeSurface extends GLSurfaceView {
        GlobeRenderer renderer;
        float downX,downY;
        public GlobeSurface(Context context){super(context);setEGLContextClientVersion(2);renderer=new GlobeRenderer(context);setRenderer(renderer);setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE){float dx=e.getX()-downX,dy=e.getY()-downY;renderer.rotate(dx*.45f,dy*.35f);downX=e.getX();downY=e.getY();return true;}
            return true;
        }
    }

    class NexusOverlay extends View {
        Canvas c;Paint p=new Paint(3);int page=0,theme=0;float downX,downY;long downAt;
        int cyan=0xff38caff,white=0xffeaf7ff,muted=0xff9bbbd6;
        String[] fav={"YouTube","Chrome","Telegram","WhatsApp","Fotos","Play Store","Config.","Câmera"};
        NexusOverlay(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        void fill(int col){p.setColor(col);p.setShader(null);p.setStyle(Paint.Style.FILL);p.clearShadowLayer();}
        void txt(String s,float x,float y,float size,int col,boolean bold){fill(col);p.setTextSize(size);p.setTypeface(Typeface.create(bold?"sans-serif-medium":"sans-serif",0));c.drawText(s,x,y,p);}
        void rr(float l,float t,float r,float b,float rad,int col,boolean stroke){fill(col);if(stroke){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);}c.drawRoundRect(l,t,r,b,rad,rad,p);p.setStyle(Paint.Style.FILL);}
        void circ(float x,float y,float r,int col,boolean stroke){fill(col);if(stroke){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);}c.drawCircle(x,y,r,p);p.setStyle(Paint.Style.FILL);}
        @Override protected void onDraw(Canvas cv){
            super.onDraw(cv);c=cv;float w=getWidth(),h=getHeight();
            // Overlay intentionally translucent so the true OpenGL globe remains visible.
            if(page==0){
                txt("◉  N E X U S",20,34,16,cyan,true);
                txt(new SimpleDateFormat("HH:mm",Locale.getDefault()).format(new Date()),22,80,38,white,false);
                txt(new SimpleDateFormat("EEE, dd MMM",new Locale("pt","BR")).format(new Date()),24,102,12,muted,false);
                txt("☀  28°C  •  Fortaleza",24,124,12,white,false);
                rr(w-132,48,w-15,111,14,0x883c89ba,true);txt("HOJE",w-118,66,9,cyan,true);txt("17:30  Reunião",w-118,83,8,white,false);txt("20:00  Academia",w-118,97,8,white,false);
                // Small orbital badges over the actual 3D globe.
                String[] glyph={"▶","♫","✉","◎","⚙","▣"};float[][] pts={{.20f,.30f},{.79f,.30f},{.23f,.41f},{.76f,.41f},{.34f,.47f},{.66f,.47f}};
                for(int i=0;i<pts.length;i++){float x=w*pts[i][0],y=h*pts[i][1];circ(x,y,16,0xb70c2847,false);circ(x,y,16,cyan,true);txt(glyph[i],x-6,y+5,14,white,true);}
                rr(19,h*.535f,w-19,h*.592f,20,0xc20b2540,true);txt("⌕",33,h*.574f,23,cyan,true);txt("Pesquisar apps, pessoas, web...",63,h*.57f,12,white,false);txt("🎙",w-45,h*.573f,14,cyan,true);
                for(int i=0;i<fav.length;i++){int row=i/4,col=i%4;float x=w*(.13f+col*.247f),y=h*(.65f+row*.09f);icon(fav[i],x,y,19);String label=fav[i].equals("Config.")?"Config.":fav[i];txt(label,x-Math.min(25,label.length()*3.1f),y+33,8.5f,white,false);}
                rr(17,h-69,w-17,h-12,23,0xd60a1d34,true);icon("Telefone",w*.16f,h-40,16);icon("Mensagens",w*.34f,h-40,16);
                circ(w*.5f,h-40,22,0xff073b67,false);circ(w*.5f,h-40,22,cyan,true);circ(w*.5f,h-40,12,0xff43caff,false);
                icon("Câmera",w*.68f,h-40,16);icon("Aplicativos",w*.85f,h-40,16);
                txt("Gire o globo • deslize para cima",w*.5f-89,h-79,9,muted,false);
            } else if(page==1) drawApps(w,h); else if(page==2) drawSettings(w,h); else if(page==3) drawWorld(w,h); else drawOrbital(w,h);
        }
        void icon(String name,float x,float y,float r){
            int co=name.equals("WhatsApp")?0xff19c766:name.equals("YouTube")?0xffff3030:name.equals("Chrome")?0xff4388ef:name.equals("Telegram")?0xff2aaee8:name.equals("Fotos")?0xffdcebf8:name.equals("Play Store")?0xff41b984:name.equals("Config.")?0xff718ba8:name.equals("Câmera")?0xff92bbd8:name.equals("Telefone")?0xff42a7ff:name.equals("Mensagens")?0xff3e92d7:0xff117fb8;
            circ(x,y,r+2,0x4433baff,false);rr(x-r,y-r,x+r,y+r,r*.38f,co,false);
            String s=name.equals("YouTube")?"▶":name.equals("Chrome")?"◉":name.equals("WhatsApp")?"☎":name.equals("Telegram")?"➤":name.equals("Fotos")?"✿":name.equals("Play Store")?"▷":name.equals("Config.")?"⚙":name.equals("Câmera")?"◉":name.equals("Telefone")?"☎":name.equals("Mensagens")?"▰":name.equals("Aplicativos")?"⁙":"•";
            txt(s,x-6,y+5,r*.82f,Color.WHITE,true);
        }
        void drawApps(float w,float h){
            txt("‹  Todos os aplicativos",20,43,21,white,true);rr(17,57,w-17,99,15,0xee0b2039,true);txt("⌕  Pesquisar apps...",30,83,13,muted,false);
            txt("Todos",23,126,11,cyan,true);txt("Recentes",w*.39f,126,11,muted,false);txt("Favoritos",w*.68f,126,11,muted,false);
            PackageManager pm=getPackageManager();ArrayList<ApplicationInfo> apps=new ArrayList<>();for(ApplicationInfo a:pm.getInstalledApplications(0))if(pm.getLaunchIntentForPackage(a.packageName)!=null)apps.add(a);
            Collections.sort(apps,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));
            float cell=w/4f;int max=(int)((h-145)/(cell+8));int n=Math.min(apps.size(),max*4);
            for(int i=0;i<n;i++){ApplicationInfo a=apps.get(i);int row=i/4,col=i%4;float x=cell*(col+.5f),y=153+row*(cell+8);Drawable d=a.loadIcon(pm);d.setBounds((int)(x-18),(int)(y-18),(int)(x+18),(int)(y+18));d.draw(c);String name=a.loadLabel(pm).toString();txt(name.length()>11?name.substring(0,10)+"…":name,x-Math.min(27,name.length()*2.8f),y+31,8.5f,white,false);}
            txt("Toque em um aplicativo para abrir",20,h-18,10,muted,false);
        }
        void drawSettings(float w,float h){
            txt("‹  Personalização",20,43,22,white,true);txt("AMBIENTES",19,77,10,cyan,true);String[] env={"Cidade","Natureza","Espaço","Minimalista"};
            for(int i=0;i<4;i++){float x=12+i*(w-24)/4;rr(x,89,x+(w-36)/4,147,10,i==theme?0xff164b78:0xff0b2035,true);txt(new String[]{"▥","♧","✦","◇"}[i],x+12,124,20,white,true);txt(env[i],x,161,8,white,false);}
            String[] rows={"Papel de parede","Tema e cores","Ícones","Efeitos de transição","Gestos inteligentes","Modo noturno dinâmico","Visão de Mundo 3D","Definir launcher padrão"};
            for(int i=0;i<rows.length;i++){float y=199+i*43;rr(13,y,w-13,y+36,10,0xee0a1b30,true);txt(new String[]{"▧","◈","⁙","✧","☝","☾","◎","⌂"}[i],24,y+23,15,cyan,true);txt(rows[i],49,y+23,11,white,false);txt("›",w-29,y+23,17,muted,false);}
        }
        void drawWorld(float w,float h){txt("‹  Visão de Mundo 3D",20,43,21,white,true);txt("A Terra é um globo 3D real",20,75,12,cyan,true);txt("Arraste com o dedo para girar",20,96,11,white,false);rr(20,h*.65f,w-20,h*.715f,18,0xdd0b2946,true);txt("⌕  Pesquisar um lugar...",36,h*.69f,12,white,false);txt("O globo continua animado ao fundo.",20,h*.75f,10,muted,false);}
        void drawOrbital(float w,float h){txt("‹  Pasta Orbital",20,43,22,white,true);txt("Atalhos orbitais",20,75,12,cyan,true);for(int i=0;i<8;i++){double a=i*Math.PI/4;float x=(float)(w/2+Math.cos(a)*w*.29),y=(float)(h*.42+Math.sin(a)*h*.12);circ(x,y,21,0xff10385d,false);circ(x,y,21,cyan,true);txt(new String[]{"▶","♫","✉","◎","⚙","▣","⌂","◆"}[i],x-7,y+6,17,white,true);}txt("Toque em um atalho para personalizar",20,h*.65f,11,white,false);}
        @Override public boolean onTouchEvent(android.view.MotionEvent e){
            float x=e.getX(),y=e.getY(),w=getWidth(),h=getHeight();
            if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;downAt=System.currentTimeMillis();return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE && page==0 && downY>getHeight()*.20f && downY<getHeight()*.53f){
                float dx=x-downX, dy=y-downY;
                globe.renderer.rotate(dx*.45f,dy*.35f);
                downX=x;downY=y;
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){
                float dx=x-downX,dy=y-downY;
                if(Math.abs(dy)>65){page=dy<0?1:0;invalidate();return true;}
                if(page!=0){
                    if(y<54&&x<110){page=0;invalidate();return true;}
                    if(page==1){if(y<105){((MainActivity)getContext()).searchDialog();return true;}PackageManager pm=getPackageManager();ArrayList<ApplicationInfo> apps=new ArrayList<>();for(ApplicationInfo a:pm.getInstalledApplications(0))if(pm.getLaunchIntentForPackage(a.packageName)!=null)apps.add(a);Collections.sort(apps,(a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));float cell=w/4f;int col=Math.min(3,(int)(x/cell)),row=Math.max(0,(int)((y-153)/(cell+8))),idx=row*4+col;if(idx>=0&&idx<apps.size()){((MainActivity)getContext()).openApp(apps.get(idx).loadLabel(pm).toString());return true;}}
                    if(page==2){if(y>88&&y<170){theme=Math.max(0,Math.min(3,(int)(x/(w/4))));}else if(y>485&&y<535){page=3;}else if(y>535&&y<590){page=4;}else if(y>500){try{startActivity(new Intent(android.provider.Settings.ACTION_HOME_SETTINGS));}catch(Exception ignored){}}invalidate();return true;}
                    if(page==3&&y>h*.62f){((MainActivity)getContext()).searchDialog();return true;}
                    if(page==4){((MainActivity)getContext()).searchDialog();return true;}
                }else{
                    if(y>h*.535f&&y<h*.60f){((MainActivity)getContext()).searchDialog();return true;}
                    if(y>h*.63f&&y<h*.84f){int row=(int)((y-h*.65f)/(h*.09f)),col=Math.min(3,Math.max(0,(int)(x/w*4)));int idx=row*4+col;if(idx>=0&&idx<fav.length){((MainActivity)getContext()).openApp(fav[idx]);return true;}}
                    if(y>h-75){if(x>w*.76f)page=1;else if(x>w*.43f&&x<w*.57f)page=2;else if(x<w*.23f)((MainActivity)getContext()).openApp("Telefone");else if(x<w*.42f)((MainActivity)getContext()).openApp("Mensagens");else ((MainActivity)getContext()).openApp("Câmera");invalidate();return true;}
                    if(y<48&&x<160){page=2;invalidate();return true;}
                    if(y>h*.22f&&y<h*.51f){page=3;invalidate();return true;}
                    if(System.currentTimeMillis()-downAt>500){page=2;invalidate();return true;}
                }
                invalidate();return true;
            }return true;
        }
    }

    class GlobeRenderer implements GLSurfaceView.Renderer {
        Context context; FloatBufferData sphere,ring; int program,texture; float rotY=0,rotX=-12; long start=System.currentTimeMillis();
        float[] projection=new float[16],view=new float[16],model=new float[16],mvp=new float[16];
        GlobeRenderer(Context ctx){context=ctx;}
        void rotate(float x,float y){rotY+=x;rotX=Math.max(-70,Math.min(70,rotX+y));}
        @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){
            GLES20.glClearColor(0.005f,0.015f,0.05f,1f);GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glEnable(GLES20.GL_BLEND);GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA,GLES20.GL_ONE_MINUS_SRC_ALPHA);
            String vs="attribute vec3 aPos; attribute vec2 aTex; uniform mat4 uMVP; varying vec2 vTex; varying float vLight; void main(){gl_Position=uMVP*vec4(aPos,1.0);vTex=aTex;vLight=0.62+0.38*max(0.0,dot(normalize(aPos),normalize(vec3(-0.4,0.5,1.0))));}";
            String fs="precision mediump float; uniform sampler2D uTexture; varying vec2 vTex; varying float vLight; void main(){vec4 t=texture2D(uTexture,vTex);gl_FragColor=vec4(t.rgb*vLight,t.a);}";
            program=makeProgram(vs,fs);sphere=new FloatBufferData(makeSphere(1f,56,42));ring=new FloatBufferData(makeRing());texture=loadTexture();
        }
        @Override public void onSurfaceChanged(GL10 gl,int width,int height){GLES20.glViewport(0,0,width,height);Matrix.perspectiveM(projection,0,43f,(float)width/height,0.1f,100f);}
        @Override public void onDrawFrame(GL10 gl){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            Matrix.setLookAtM(view,0,0,0,4.6f,0,0,0,0,1,0);
            Matrix.setIdentityM(model,0);Matrix.translateM(model,0,0,-0.15f,0);
            Matrix.rotateM(model,0,rotX,1,0,0);Matrix.rotateM(model,0,rotY+(System.currentTimeMillis()-start)*0.006f,0,1,0);
            Matrix.scaleM(model,0,1.03f,1.03f,1.03f);Matrix.multiplyMM(mvp,0,view,0,model,0);Matrix.multiplyMM(mvp,0,projection,0,mvp,0);
            GLES20.glUseProgram(program);int pos=GLES20.glGetAttribLocation(program,"aPos"),tex=GLES20.glGetAttribLocation(program,"aTex"),um=GLES20.glGetUniformLocation(program,"uMVP");
            GLES20.glUniformMatrix4fv(um,1,false,mvp,0);GLES20.glActiveTexture(GLES20.GL_TEXTURE0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture);GLES20.glUniform1i(GLES20.glGetUniformLocation(program,"uTexture"),0);
            sphere.draw(pos,tex);
            // glowing orbital ellipses rendered as real 3D line geometry
            int flat=flatProgram();GLES20.glUseProgram(flat);int fp=GLES20.glGetAttribLocation(flat,"aPos"),fm=GLES20.glGetUniformLocation(flat,"uMVP"),fc=GLES20.glGetUniformLocation(flat,"uColor");
            float[] orbitModel=new float[16],orbitMvp=new float[16];Matrix.setIdentityM(orbitModel,0);Matrix.translateM(orbitModel,0,0,-.15f,0);Matrix.rotateM(orbitModel,0,rotX,1,0,0);Matrix.rotateM(orbitModel,0,rotY+(System.currentTimeMillis()-start)*.006f,0,1,0);
            Matrix.multiplyMM(orbitMvp,0,view,0,orbitModel,0);Matrix.multiplyMM(orbitMvp,0,projection,0,orbitMvp,0);GLES20.glUniformMatrix4fv(fm,1,false,orbitMvp,0);
            GLES20.glUniform4f(fc,.12f,.65f,1f,.62f);ring.drawLines(fp);
            GLES20.glUniform4f(fc,.55f,.18f,1f,.40f);ring.drawLines(fp);
        }
        int makeProgram(String vs,String fs){int v=shader(GLES20.GL_VERTEX_SHADER,vs),f=shader(GLES20.GL_FRAGMENT_SHADER,fs),pr=GLES20.glCreateProgram();GLES20.glAttachShader(pr,v);GLES20.glAttachShader(pr,f);GLES20.glLinkProgram(pr);return pr;}
        int shader(int type,String src){int s=GLES20.glCreateShader(type);GLES20.glShaderSource(s,src);GLES20.glCompileShader(s);return s;}
        int flatProgram(){return FlatHolder.program(this);}
        int loadTexture(){int[] ids=new int[1];GLES20.glGenTextures(1,ids,0);GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,ids[0]);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_LINEAR);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_REPEAT);GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);android.graphics.Bitmap b=android.graphics.BitmapFactory.decodeResource(getResources(),com.nexus.launcher.R.drawable.earth_texture);android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,b,0);b.recycle();return ids[0];}
        float[] makeSphere(float r,int stacks,int slices){ArrayList<Float> a=new ArrayList<>();for(int i=0;i<stacks;i++){float lat0=(float)(Math.PI*(-.5+(double)i/stacks)),lat1=(float)(Math.PI*(-.5+(double)(i+1)/stacks));for(int j=0;j<slices;j++){float lon0=(float)(2*Math.PI*(double)j/slices),lon1=(float)(2*Math.PI*(double)(j+1)/slices);vertex(a,r,lat0,lon0);vertex(a,r,lat1,lon0);vertex(a,r,lat1,lon1);vertex(a,r,lat0,lon0);vertex(a,r,lat1,lon1);vertex(a,r,lat0,lon1);}}float[] out=new float[a.size()];for(int i=0;i<out.length;i++)out[i]=a.get(i);return out;}
        void vertex(ArrayList<Float> a,float r,float lat,float lon){float x=(float)(r*Math.cos(lat)*Math.cos(lon)),y=(float)(r*Math.sin(lat)),z=(float)(r*Math.cos(lat)*Math.sin(lon));a.add(x);a.add(y);a.add(z);a.add(lon/(float)(2*Math.PI)+.5f);a.add(lat/(float)Math.PI+.5f);}
        float[] makeRing(){ArrayList<Float> a=new ArrayList<>();for(int plane=0;plane<2;plane++)for(int i=0;i<=240;i++){double t=2*Math.PI*i/240;float x=(float)(1.36*Math.cos(t)),y=(float)(plane==0?.39*Math.sin(t):.60*Math.sin(t)),z=(float)(plane==0?.60*Math.sin(t):.39*Math.sin(t));a.add(x);a.add(y);a.add(z);}float[] o=new float[a.size()];for(int i=0;i<o.length;i++)o[i]=a.get(i);return o;}
    }
    static class FlatHolder {
        static int program;
        static int program(GlobeRenderer r){if(program!=0)return program;String vs="attribute vec3 aPos;uniform mat4 uMVP;void main(){gl_Position=uMVP*vec4(aPos,1.0);}";String fs="precision mediump float;uniform vec4 uColor;void main(){gl_FragColor=uColor;}";program=r.makeProgram(vs,fs);return program;}
    }
    static class FloatBufferData {
        java.nio.FloatBuffer buf;int stride,vertices;boolean textured;
        FloatBufferData(float[] a){buf=ByteBuffer.allocateDirect(a.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();buf.put(a).position(0);textured=(a.length%5==0);stride=textured?20:12;vertices=a.length/(stride/4);}
        void draw(int pos,int tex){buf.position(0);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,stride,buf);buf.position(3);GLES20.glEnableVertexAttribArray(tex);GLES20.glVertexAttribPointer(tex,2,GLES20.GL_FLOAT,false,stride,buf);GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,vertices);buf.position(0);}
        void drawLines(int pos){buf.position(0);GLES20.glEnableVertexAttribArray(pos);GLES20.glVertexAttribPointer(pos,3,GLES20.GL_FLOAT,false,12,buf);GLES20.glLineWidth(2f);GLES20.glDrawArrays(GLES20.GL_LINE_STRIP,0,vertices);buf.position(0);}
    }
}
