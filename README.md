# MetalSlug-合金弹头安卓版
该项目是疯狂Android讲义（第三版）第十八章的游戏程序，所有组件代码都基本与书中无异。
## 游戏截图：
![](https://github.com/Serene-Seven/MetalSlug/raw/master/Screenshots/1.png)
![](https://github.com/Serene-Seven/MetalSlug/raw/master/Screenshots/2.png)
![](https://github.com/Serene-Seven/MetalSlug/raw/master/Screenshots/4.png)
![](https://github.com/Serene-Seven/MetalSlug/raw/master/Screenshots/3.png)
然而该项目瑕疵很多，错误也较多，毕竟已经是旧安卓版本的一个项目了。  
我还只是个安卓小白，目前还有很多对该项目不满意的地方不知道从何修改，日后如果有那个能力了说不定能把这个项目优化成一个尚且能玩的合金弹头。
## 已修复的BUG有：
①、角色在跳跃的时候，再不断按跳跃能使角色一直卡在空中漂移。
## 还未修复的BUG有：
①、游戏界面上方有一个很丑的标题栏不知道怎么去掉。（使用android:theme="@android:style/Theme.NoTitleBar"会导致游戏打不开闪退)  
②、最致命的，怪物无法生成，明明怪物管理器设有两种敌人的（一种人型，一种飞机型），然而没加载出来，导致游戏成为一个只有玩家而没有敌人的空壳，原因尚不明。  
③、如果发射子弹后再按跳跃按钮，子弹也会跟着往上走，即玩家的跳跃赋予了已经发射的子弹一个Y轴上的速度。而我们的本意是先跳跃再发射子弹可以让子弹往右上走。  
④、游戏背景图片的加载随着玩家的移动可能会出现一块区域是全黑的，虽然之后会填补，并且不影响玩家走动，但是强迫症不能忍，应该是循环加载背景的时候有什么缺陷。
