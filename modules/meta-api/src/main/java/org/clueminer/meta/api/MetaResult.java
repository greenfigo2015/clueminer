/*
 * Copyright (C) 2011-2016 clueminer.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.clueminer.meta.api;

import java.io.Serializable;

/**
 * Simple class for exchanging clustering results
 *
 * @author Tomas Barton
 */
public class MetaResult implements Serializable {

    private static final long serialVersionUID = -6611537651936473664L;

    String template;

    double score;

    MetaFlag flag = MetaFlag.NONE;

    String fingerprint;

    /**
     * Java hashcode of the clustering
     */
    int hash;

    /**
     * number of partitions
     */
    int k;

    public MetaResult(int k, String template, double score, String fingerprint, int hash) {
        this.k = k;
        this.template = template;
        this.score = score;
        this.fingerprint = fingerprint;
        this.hash = hash;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public MetaFlag getFlag() {
        return flag;
    }

    public void setFlag(MetaFlag flag) {
        this.flag = flag;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public int getHash() {
        return hash;
    }

    public void setHash(int hash) {
        this.hash = hash;
    }

}
