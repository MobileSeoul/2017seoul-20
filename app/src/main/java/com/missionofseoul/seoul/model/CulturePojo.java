package com.missionofseoul.seoul.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hyunho on 2017-10-09.
 */

public class CulturePojo implements Serializable {

    @SerializedName("SearchConcertDetailService")
    @Expose
    private SearchConcertDetailService searchConcertDetailService;

    public SearchConcertDetailService getSearchConcertDetailService() {
        return searchConcertDetailService;
    }

    public void setSearchConcertDetailService(SearchConcertDetailService searchConcertDetailService) {
        this.searchConcertDetailService = searchConcertDetailService;
    }

    public class RESULT implements Serializable {

        @SerializedName("CODE")
        @Expose
        private String cODE;
        @SerializedName("MESSAGE")
        @Expose
        private String mESSAGE;

        public String getCODE() {
            return cODE;
        }

        public void setCODE(String cODE) {
            this.cODE = cODE;
        }

        public String getMESSAGE() {
            return mESSAGE;
        }

        public void setMESSAGE(String mESSAGE) {
            this.mESSAGE = mESSAGE;
        }

    }

    public class Row implements Serializable {

        @SerializedName("CULTCODE")
        @Expose
        private Double cULTCODE;
        @SerializedName("SUBJCODE")
        @Expose
        private Double sUBJCODE;
        @SerializedName("CODENAME")
        @Expose
        private String cODENAME;
        @SerializedName("TITLE")
        @Expose
        private String tITLE;
        @SerializedName("STRTDATE")
        @Expose
        private String sTRTDATE;
        @SerializedName("END_DATE")
        @Expose
        private String eNDDATE;
        @SerializedName("TIME")
        @Expose
        private String tIME;
        @SerializedName("PLACE")
        @Expose
        private String pLACE;
        @SerializedName("ORG_LINK")
        @Expose
        private String oRGLINK;
        @SerializedName("MAIN_IMG")
        @Expose
        private String mAINIMG;
        @SerializedName("HOMEPAGE")
        @Expose
        private String hOMEPAGE;
        @SerializedName("USE_TRGT")
        @Expose
        private String uSETRGT;
        @SerializedName("USE_FEE")
        @Expose
        private String uSEFEE;
        @SerializedName("SPONSOR")
        @Expose
        private String sPONSOR;
        @SerializedName("INQUIRY")
        @Expose
        private String iNQUIRY;
        @SerializedName("SUPPORT")
        @Expose
        private String sUPPORT;
        @SerializedName("ETC_DESC")
        @Expose
        private String eTCDESC;
        @SerializedName("AGELIMIT")
        @Expose
        private String aGELIMIT;
        @SerializedName("IS_FREE")
        @Expose
        private String iSFREE;
        @SerializedName("TICKET")
        @Expose
        private String tICKET;
        @SerializedName("PROGRAM")
        @Expose
        private String pROGRAM;
        @SerializedName("PLAYER")
        @Expose
        private String pLAYER;
        @SerializedName("CONTENTS")
        @Expose
        private String cONTENTS;
        @SerializedName("GCODE")
        @Expose
        private String gCODE;

        public Double getCULTCODE() {
            return cULTCODE;
        }

        public void setCULTCODE(Double cULTCODE) {
            this.cULTCODE = cULTCODE;
        }

        public Double getSUBJCODE() {
            return sUBJCODE;
        }

        public void setSUBJCODE(Double sUBJCODE) {
            this.sUBJCODE = sUBJCODE;
        }

        public String getCODENAME() {
            return cODENAME;
        }

        public void setCODENAME(String cODENAME) {
            this.cODENAME = cODENAME;
        }

        public String getTITLE() {
            return tITLE;
        }

        public void setTITLE(String tITLE) {
            this.tITLE = tITLE;
        }

        public String getSTRTDATE() {
            return sTRTDATE;
        }

        public void setSTRTDATE(String sTRTDATE) {
            this.sTRTDATE = sTRTDATE;
        }

        public String getENDDATE() {
            return eNDDATE;
        }

        public void setENDDATE(String eNDDATE) {
            this.eNDDATE = eNDDATE;
        }

        public String getTIME() {
            return tIME;
        }

        public void setTIME(String tIME) {
            this.tIME = tIME;
        }

        public String getPLACE() {
            return pLACE;
        }

        public void setPLACE(String pLACE) {
            this.pLACE = pLACE;
        }

        public String getORGLINK() {
            return oRGLINK;
        }

        public void setORGLINK(String oRGLINK) {
            this.oRGLINK = oRGLINK;
        }

        public String getMAINIMG() {
            return mAINIMG;
        }

        public void setMAINIMG(String mAINIMG) {
            this.mAINIMG = mAINIMG;
        }

        public String getHOMEPAGE() {
            return hOMEPAGE;
        }

        public void setHOMEPAGE(String hOMEPAGE) {
            this.hOMEPAGE = hOMEPAGE;
        }

        public String getUSETRGT() {
            return uSETRGT;
        }

        public void setUSETRGT(String uSETRGT) {
            this.uSETRGT = uSETRGT;
        }

        public String getUSEFEE() {
            return uSEFEE;
        }

        public void setUSEFEE(String uSEFEE) {
            this.uSEFEE = uSEFEE;
        }

        public String getSPONSOR() {
            return sPONSOR;
        }

        public void setSPONSOR(String sPONSOR) {
            this.sPONSOR = sPONSOR;
        }

        public String getINQUIRY() {
            return iNQUIRY;
        }

        public void setINQUIRY(String iNQUIRY) {
            this.iNQUIRY = iNQUIRY;
        }

        public String getSUPPORT() {
            return sUPPORT;
        }

        public void setSUPPORT(String sUPPORT) {
            this.sUPPORT = sUPPORT;
        }

        public String getETCDESC() {
            return eTCDESC;
        }

        public void setETCDESC(String eTCDESC) {
            this.eTCDESC = eTCDESC;
        }

        public String getAGELIMIT() {
            return aGELIMIT;
        }

        public void setAGELIMIT(String aGELIMIT) {
            this.aGELIMIT = aGELIMIT;
        }

        public String getISFREE() {
            return iSFREE;
        }

        public void setISFREE(String iSFREE) {
            this.iSFREE = iSFREE;
        }

        public String getTICKET() {
            return tICKET;
        }

        public void setTICKET(String tICKET) {
            this.tICKET = tICKET;
        }

        public String getPROGRAM() {
            return pROGRAM;
        }

        public void setPROGRAM(String pROGRAM) {
            this.pROGRAM = pROGRAM;
        }

        public String getPLAYER() {
            return pLAYER;
        }

        public void setPLAYER(String pLAYER) {
            this.pLAYER = pLAYER;
        }

        public String getCONTENTS() {
            return cONTENTS;
        }

        public void setCONTENTS(String cONTENTS) {
            this.cONTENTS = cONTENTS;
        }

        public String getGCODE() {
            return gCODE;
        }

        public void setGCODE(String gCODE) {
            this.gCODE = gCODE;
        }

    }

    public class SearchConcertDetailService implements Serializable {

        @SerializedName("list_total_count")
        @Expose
        private Integer listTotalCount;
        @SerializedName("RESULT")
        @Expose
        private RESULT rESULT;
        @SerializedName("row")
        @Expose
        private List<Row> row = null;

        public Integer getListTotalCount() {
            return listTotalCount;
        }

        public void setListTotalCount(Integer listTotalCount) {
            this.listTotalCount = listTotalCount;
        }

        public RESULT getRESULT() {
            return rESULT;
        }

        public void setRESULT(RESULT rESULT) {
            this.rESULT = rESULT;
        }

        public List<Row> getRow() {
            return row;
        }

        public void setRow(List<Row> row) {
            this.row = row;
        }

    }
}//end of class
