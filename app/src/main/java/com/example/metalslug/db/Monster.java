package com.example.metalslug.db;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.example.metalslug.GameView;
import com.example.metalslug.ViewManager;
import com.example.metalslug.util.Graphics;
import com.example.metalslug.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/2/27.
 */

public class Monster {
    //定义代表怪物类型的常量
    public static final int TYPE_BOMB = 1;
    public static final int TYPE_FLY = 2;
    public static final int TYPE_MAN = 3;
    //定义怪物类型的成员常量
    private int type = TYPE_BOMB;
    //定义怪物X、Y坐标的成员变量
    private int x = 0;
    private int y = 0;
    //定义怪物是否已经死亡的旗标
    private boolean isDie = false;
    //绘制怪物图片左上角的X，Y坐标。
    private int startX = 0;
    private int startY = 0;
    //绘制怪物图片右下角的X，Y坐标。
    private int endX = 0;
    private int endY = 0;
    //控制动画刷新的速度
    int drawCount = 0;
    //定义当前正在绘制怪物的动画第几帧
    private int drawIndex = 0;
    //用于记录死亡动画只绘制一次，不重复绘制
    //每当怪物死亡时，该变量会被初始化为等于死亡动画的总帧数
    //当怪物的死亡动画帧播放完成时，该变量的值变为0
    private int dieMaxDrawCount = Integer.MAX_VALUE;
    //定义怪物射出的子弹
    private List<Bullet> bulletList = new ArrayList<>();
    /**
     * 上面的成员变量即可记录该怪物实例的各种状态。实际上以后程序要升级，
     * 比如为怪物增加更多的特征，如怪物可以拿不同的武器，怪物可以穿不同的衣服，怪物可以具有不同的攻击力……
     * 这些都可考虑定义成怪物的成员变量。
     */

    //构造函数，传入type告知该怪物是什么类型即可。
    public Monster(int type) {
        this.type = type;
        //下面代码根据怪物类型来初始化怪物X、Y坐标
        //如果怪物是炸弹或敌人，怪物的Y坐标与玩家控制的角色的Y坐标相同，纵坐标相同，处在同一水平面。
        if (type == TYPE_BOMB || type == TYPE_MAN) {
            y = Player.Y_DEFALUT;
        } else if (type == TYPE_FLY) {
            y = ViewManager.SCREEN_HEIGHT * 50 / 100 - Util.rand((int)(ViewManager.scale * 100));
        }
        //不管什么怪物，都随机计算怪物的X坐标
        y = ViewManager.SCREEN_WIDTH + Util.rand(ViewManager.SCREEN_WIDTH >> 1) - (ViewManager.SCREEN_WIDTH >> 2);
    }

    //采用后台线程来控制不断地绘制怪物动画的下一帧，每次都只绘制一帧
    //绘制怪物，根据不同类型与死亡与否传入不同的位图数组，每个位图数组都含有一组动画帧的所有位图。
    //真正绘制发生在drawAni
    public void draw(Canvas canvas) {
        if (canvas == null) {
            return;
        }
        switch (type) {
            case TYPE_BOMB:
                //判断是否死亡，死亡的就绘制死亡的图片
                drawAni(canvas, isDie ? ViewManager.bomb2Image : ViewManager.bombImage);
                break;
            case TYPE_FLY:
                drawAni(canvas, isDie ? ViewManager.flyDieImage : ViewManager.flyImage);
                break;
            case TYPE_MAN:
                drawAni(canvas, isDie ? ViewManager.manDieImage : ViewManager.manImgae);
                break;
            default:
                break;
        }
    }

    //根据怪物的动画帧图片来绘制怪物动画，其实这就是每一帧
    public void drawAni(Canvas canvas, Bitmap[] bitmapArr) {
        if (canvas == null) {
            return;
        }
        if (bitmapArr == null) {
            return;
        }
        //如果怪物已经死，且没有播放过死亡动画
        //dieMaxDrawCount等于初始值表明未播放过死亡动画
        if (isDie && dieMaxDrawCount == Integer.MAX_VALUE)
        {
            //将dieMaxDrawCount设置与死亡动画的总帧数相等
            dieMaxDrawCount = bitmapArr.length;  //⑤
        }
        //取余是因为移动是一个重复的过程，不断提取下一帧然后到动画结尾又要重新开始一帧了
        drawIndex = drawIndex % bitmapArr.length;
        //获取当前绘制的动画帧对应的位图
        Bitmap bitmap = bitmapArr[drawIndex];  //①
        //如果获得的位图为空或者位图已经被回收了
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }

        int drawX = x;
        //对绘制怪物动画帧位图的X坐标进行微调
        if (isDie) {
            if (type == TYPE_BOMB) {
                drawX = x - (int) (ViewManager.scale * 50);
            } else if (type == TYPE_MAN) {
                drawX = x + (int) (ViewManager.scale * 50);
            }
        }
        //对绘制怪物动画帧位图的Y坐标进行微调
        int drawY = y - bitmap.getHeight();

        //万事具备，这一步画怪物动画帧的位图
        Graphics.drawMatrixImage(canvas, bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), Graphics.TRANS_NONE, drawX, drawY, 0,
                Graphics.TIMES_SCALE);
        startX = drawX;
        startY = drawY;
        endX = startX + bitmap.getWidth();
        endY = startY + bitmap.getHeight();
        drawCount++;
        //后面6、4用于控制人、飞机的发射子弹的速度，详情看下面注释
        if (drawCount >= (type == TYPE_MAN ? 6 : 4)) { //③
            // 如果怪物是人，只在第3帧才发射子弹
            if (type == TYPE_MAN && drawIndex == 2) {
                addBullet();
            }
            // 如果怪物是飞机，只在最后一帧才发射子弹
            if (type == TYPE_FLY && drawIndex == bitmapArr.length - 1) {
                addBullet();
            }
            //只有执行了6/4次drawAni()也就是drawCount++后，才会进行下一帧。
            drawIndex++;  //②
            //然后计数器清零
            drawCount = 0;  //④
        }
        //如果已经死了，开始播放死亡动画，每播放死亡动画的一帧，dieMaxDrawCount减1。
        //当dieMaxDrawCount等于0时，表明死亡动画播放完成，MonsterManger会删除该怪物。
        if (isDie) {
            dieMaxDrawCount--;  //⑥
        }
        // 绘制子弹
        drawBullet(canvas);
    }

    //startX startY endX endY这四个变量代表了怪物当前帧所覆盖的矩形区域，如果子弹出现在该矩阵区域内，说明打中了
    //判断怪物是否被子弹打中的方法
    public boolean isHurt(int x, int y) {
        return x >= startX && x <= endX && y >= startY && y <= endY;
    }

    //根据怪物类型获取子弹类型，不同怪物发射不同的子弹
    //return 0代表这种怪物不发射子弹
    public int getBulletType() {
        switch (type) {
            case TYPE_BOMB:
                return 0;
            case TYPE_FLY:
                return Bullet.BULLET_TYPE_3;
            case TYPE_MAN:
                return Bullet.BULLET_TYPE_2;
            default:
                return 0;
        }
    }

    //定义发射子弹的方法
    public void addBullet() {
        //先判断发射的子弹类型
        int bulletType = getBulletType();
        //如果不发射子弹
        if (bulletType <= 0) {
            return;
        }
        //计算子弹的X、Y坐标
        int drawX = x;
        int drawY = y - (int) (ViewManager.scale * 60);
        //如果怪物是飞机，重新计算飞机发射的子弹的Y坐标
        if (type == TYPE_FLY) {
            drawY = y - (int) (ViewManager.scale * 30);
        }
        //创建子弹对象
        Bullet bullet = new Bullet(bulletType, drawX, drawY, Player.DIR_LEFT);
        //将子弹添加到该怪物发射的子弹集合中
        bulletList.add(bullet);
    }

    //重新绘制所有对象的位置
    //因为界面上角色会不断地向右移动，角色会产生一个shift偏移，所以程序就需要将怪物（包括它的所有子弹）全部左移shift距离，这样才会产生逼真的效果
    //更新角色的位置：将角色的X坐标减少shift距离（角色左移）
    //更新所有子弹的位置：将所有子弹的X坐标减少shift距离（子弹左移）
    public void updateShift(int shift) {
        x -= shift;
        for (Bullet bullet : bulletList) {
            if (bullet == null) {
                continue;
            }
            bullet.setX(bullet.getX() - shift);
        }
    }

    //绘制子弹
    public void drawBullet(Canvas canvas) {
        //定义一个deleteList集合，该集合保存所有需要删除的子弹
        List<Bullet> deleteList = new ArrayList<>();
        Bullet bullet = null;
        //对所有子弹校检，如果有不合格的子弹就剔除
        for (int i = 0; i < bulletList.size(); i++) {
            bullet = bulletList.get(i);
            if (bullet == null) {
                continue;
            }
            //如果子弹已经越过屏幕
            if (bullet.getX() < 0 || bullet.getX() > ViewManager.SCREEN_WIDTH) {
                //将需要清除的子弹添加到deleteList集合中
                deleteList.add(bullet);
            }
        }
        //从该怪物当前的所有子弹中删除所有需要清除的子弹
        bulletList.removeAll(deleteList);  // ⑦
        //定义代表子弹的位图
        Bitmap bitmap;
        //遍历该怪物发射的所有子弹
        for (int i = 0; i < bulletList.size(); i++) {
            bullet = bulletList.get(i);
            if (bullet == null) {
                continue;
            }
            //获取子弹对应的位图
            bitmap = bullet.getBitmap();
            if (bitmap == null) {
                continue;
            }
            //子弹移动
            bullet.move();
            //绘制子弹的位图
            Graphics.drawMatrixImage(canvas, bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), bullet.getDir() == Player.DIR_RIGHT ?
                            Graphics.TRANS_MIRROR : Graphics.TRANS_NONE,
                    bullet.getX(), bullet.getY(), 0, Graphics.TIMES_SCALE);
        }
    }

    //判断子弹是否与玩家控制的角色碰撞（判断子弹是否打中角色，打中则删除）
    public void checkBullet() {
        //定义一个delBulletList集合，该集合保存打中角色的子弹，它们将要被删除
        List<Bullet> delBulletList = new ArrayList<>();
        //遍历所有子弹
        for (Bullet bullet : bulletList) {
            //子弹无效
            if (bullet == null || !bullet.isEffect()) {
                continue;
            }
            //如果玩家控制的角色被子弹打到
            if (GameView.player.isHurt(bullet.getX(), bullet.getX()
                    , bullet.getY(), bullet.getY())) {
                //子弹设为无效
                bullet.setEffect(false);
                //将玩家的生命值减5
                GameView.player.setHp(GameView.player.getHp() - 5);
                //将子弹添加到delBulletList集合中
                delBulletList.add(bullet);
            }
        }
        //删除所有打中角色的子弹
        bulletList.removeAll(delBulletList);
    }

    //----------下面是各成员变量的setter、getter方法----------
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public boolean isDie() {
        return isDie;
    }

    public void setDie(boolean die) {
        isDie = die;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public int getEndX() {
        return endX;
    }

    public void setEndX(int endX) {
        this.endX = endX;
    }

    public int getEndY() {
        return endY;
    }

    public void setEndY(int endY) {
        this.endY = endY;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public void setDrawCount(int drawCount) {
        this.drawCount = drawCount;
    }

    public int getDrawIndex() {
        return drawIndex;
    }

    public void setDrawIndex(int drawIndex) {
        this.drawIndex = drawIndex;
    }

    public int getDieMaxDrawCount() {
        return dieMaxDrawCount;
    }

    public void setDieMaxDrawCount(int dieMaxDrawCount) {
        this.dieMaxDrawCount = dieMaxDrawCount;
    }

    public List<Bullet> getBulletList() {
        return bulletList;
    }

    public void setBulletList(List<Bullet> bulletList) {
        this.bulletList = bulletList;
    }

}
