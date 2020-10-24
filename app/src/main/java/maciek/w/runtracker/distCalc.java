package maciek.w.runtracker;

/**
 * Created by Maciek on 13.10.2020
 */
public class distCalc {

    private Double latStart;
    private Double lonStart;
    private Double latStop;
    private Double lonStop;
    private Double dist;
    private Double R;

    public distCalc(Double _latStart, Double _lonStart, Double _latStop, Double _lonStop){
        this.latStart = _latStart;
        this.lonStart = _lonStart;
        this.latStop = _latStop;
        this.lonStop = _lonStop;
        this.dist=0.0;
        this.R = 6371000.0;

        calculateDistance();
    }

    public void setLoc(Double _latStart, Double _lonStart, Double _latStop, Double _lonStop){
        this.latStart = _latStart;
        this.lonStart = _lonStart;
        this.latStop = _latStop;
        this.lonStop = _lonStop;

        calculateDistance();
    }

    public Double getDist(){
        return dist;
    }

    private void calculateDistance(){

        Double fi1 = latStart * Math.PI/180;
        Double fi2 = latStop * Math.PI/180;
        Double dfi = (latStop - latStart) * Math.PI/180;
        Double dlb = (lonStop - lonStart) * Math.PI/180;

        Double a = Math.sin(dfi/2) * Math.sin(dfi/2) +
                Math.cos(fi1) * Math.cos(fi2) * Math.sin(dlb/2) * Math.sin(dlb/2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        dist = R * c;


    }

}
