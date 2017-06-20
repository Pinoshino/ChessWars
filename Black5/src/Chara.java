import java.awt.*;
import java.util.*;

import javax.swing.*;

public class Chara {

 //キャラクター番号
  public int charaNo;

  public String name;

  //座標
  private int x, y;

  private int ap;

  private int hp;

  //private String jpg;

  public Chara(int x, int y, int ap , int hp, int charaNo, String name){

     this.x=x;
     this.y=y;

     this.ap=ap;
     this.hp=hp;

     this.charaNo = charaNo;

     this.name = name;
  }

  public void moveChara(int movex , int movey){

    x = movex;
    y = movey;

  }

  public void damage(int attack){

    hp = hp - attack;

  }


  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public int getAp() {
    return ap;
  }

  public int getHp() {
    return hp;
  }

  public int getNo(){
    return charaNo;
  }

  public String getName(){
    return name;
  }


}
