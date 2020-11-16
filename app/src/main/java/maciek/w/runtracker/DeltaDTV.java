package maciek.w.runtracker;

/**
 * Created by Maciek on 24.10.2020
 */
class DeltaDTV {
    private Double dDist;
    private Double dTime;
    private Double dVel;

    public DeltaDTV(Double _dist, Double _time) {
        this.dDist = _dist;
        this.dTime = _time;
        calcVel();
        roundValues(4.0);
    }

    private void calcVel() {
        this.dVel = this.dDist / this.dTime;
    }

    public Double getdDist() {
        return this.dDist;
    }

    public Double getdTime() {
        return this.dTime;
    }

    public Double getdVel() {
        return this.dVel;
    }

    private void roundValues(double decimal) {
        double temp = Math.pow(10.0,decimal);
        this.dDist=Math.round(dDist*temp)/temp;
        this.dVel=Math.round(dVel*temp)/temp;
    }

    public String dTimeToString(){
        return String.valueOf(dTime);
    }

    public String dDistToString(){
        return String.valueOf(dDist);
    }

    public String dVelToString(){
        return String.valueOf(dVel);
    }
}