package maciek.w.runtracker;

/**
 * Created by Maciek on 24.10.2020
 */
class DeltaDTV {
    private Double dDist;
    private Double dTime;
    private Double dVel;

    public DeltaDTV(Double _dist, Double _time){
        this.dDist=_dist;
        this.dTime=_time;
        calcVel();
    }

    private void calcVel(){
        this.dVel=this.dDist/this.dTime;
    }

    public Double getdDist(){
        return this.dDist;
    }
    public Double getdTime(){
        return this.dTime;
    }
    public Double getdVel(){
        return this.dVel;
    }
}
