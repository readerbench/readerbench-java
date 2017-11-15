/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webService.enea;

/**
 *
 * @author Gabriel Gutu <gabriel.gutu at cs.pub.ro>
 */
public class LessonExpertise {
    
    private boolean medicinePaeditrician;
    private boolean medicineGynocologist;
    private boolean medicineGp;
    private boolean medicineOther;
    private boolean nursing;
    private boolean nutrition;

    public LessonExpertise() {
        medicinePaeditrician = false;
        medicineGynocologist = false;
        medicineGp = false;
        medicineOther = false;
        nursing = false;
        nutrition = false;
    }

    public boolean isMedicinePaeditrician() {
        return medicinePaeditrician;
    }

    public boolean isMedicineGynocologist() {
        return medicineGynocologist;
    }

    public boolean isMedicineGp() {
        return medicineGp;
    }

    public boolean isMedicineOther() {
        return medicineOther;
    }

    public boolean isNursing() {
        return nursing;
    }

    public boolean isNutrition() {
        return nutrition;
    }

    public void setMedicinePaeditrician(boolean medicinePaeditrician) {
        this.medicinePaeditrician = medicinePaeditrician;
    }

    public void setMedicineGynocologist(boolean medicineGynocologist) {
        this.medicineGynocologist = medicineGynocologist;
    }

    public void setMedicineGp(boolean medicineGp) {
        this.medicineGp = medicineGp;
    }

    public void setMedicineOther(boolean medicineOther) {
        this.medicineOther = medicineOther;
    }

    public void setNursing(boolean nursing) {
        this.nursing = nursing;
    }

    public void setNutrition(boolean nutrition) {
        this.nutrition = nutrition;
    }
    
    public static Integer expertiseToConstant(String expertise) {
        switch(expertise) {
            case "medicine_paediatrician":
                return Constants.EXPERTISE_MED_PAEDI;
            case "medicine_gynocologist":
                return Constants.EXPERTISE_MED_GYNO;
            case "medicine_gp":
                return Constants.EXPERTISE_MED_GP;
            case "medicine_other":
                return Constants.EXPERTISE_MED_OTHER;
            case "nursing":
                return Constants.EXPERTISE_NURSE;
            case "nutrition":
                return Constants.EXPERTISE_NUTRI;
            default: return 0;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[EXPERTISE: ");
        if (medicinePaeditrician) sb.append("Medicine Paeditrician, ");
        if (medicineGynocologist) sb.append("Medicine Gynocologist, ");
        if (medicineGp) sb.append("Medicine GP, ");
        if (medicineOther) sb.append("Medicine Other, ");
        if (nursing) sb.append("Nursing, ");
        if (nutrition) sb.append("Nutrition, ");
        sb.append("] ");
        return sb.toString();
    }
    
}
