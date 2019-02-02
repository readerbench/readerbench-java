package com.readerbench.datasourceprovider.elasticsearch;

public class Game {

    String name;
    Integer metascore;
    Float userscore;

    public Game(String name, Integer metascore, Float userscore) {
        this.name = name;
        this.metascore = metascore;
        this.userscore = userscore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMetascore() {
        return metascore;
    }

    public void setMetascore(Integer metascore) {
        this.metascore = metascore;
    }

    public Float getUserscore() {
        return userscore;
    }

    public void setUserscore(Float userscore) {
        this.userscore = userscore;
    }
}
