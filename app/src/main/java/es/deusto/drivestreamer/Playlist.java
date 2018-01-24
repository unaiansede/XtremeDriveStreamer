package es.deusto.drivestreamer;

/**
 * Created by asiir on 21/01/2018.
 */


public class Playlist {
     public String Nombre="";
     public String[]Canciones={""};
     public String Id="";

     public Playlist(String nombre,String[] canciones,String id){
         this.Nombre=nombre;
         this.Canciones=canciones;
         this.Id=id;

     }
    public Playlist(Playlist p){
         this.Canciones=p.Canciones;
         this.Nombre=p.Nombre;
         this.Id=p.Id;

    }
    public Playlist (String s){
        String nombre="";
        String id ="";
        String[] canciones={""};
        String[] parts= s.split("/");
        this.Nombre=parts[0];
        this.Id=parts[1];
        this.Canciones=parts[3];



    }
    public String getNombre(Playlist p){
        return p.Nombre;
    }
    public String getId(Playlist p){
        return p.Id;
    }
    public String[] getCanciones(Playlist p){
        return p.Canciones;
    }public void setNombre(String nombre){
        this.Nombre=nombre;

    }public void setId(String id){
        this.Id=id;
    }public void setCanciones(String[] canciones){
        this.Canciones=canciones;
    }
    public String toString(Playlist p){
        return this.Nombre+"/"+this.Id+"/"+this.Canciones;
    }



}
