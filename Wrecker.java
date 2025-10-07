   package wrecker;
   import robocode.*;
   import robocode.util.*;
   import java.awt.geom.*;
   import java.util.*;
   import java.awt.*;


    public class Wrecker extends AdvancedRobot
   {
      //A variável direction é usada para armazenar a direção do robô.
      static double direction = 1;
      final static double angleScale = 24;
      final static double velocityScale = 1;
      static double lastEnemyHeading;
      static double lastEnemyEnergy;
      //A variável flat é usada para determinar se o robô deve se aproximar ou se afastar do oponente.
      static boolean flat;
      static boolean firstScan;
      // A variável data é um StringBuilder estático que serve
      // para armazenar informações sobre o histórico de movimentos do oponente. 
      // Ela é usada para calcular a posição prevista do oponente com 
      //base em seu histórico de movimentos... Calculados durante a batalha
      static StringBuilder data = new StringBuilder();
      static double bulletVelocity;      
   	
       public void run() {
	   // Definir as cores
	   setColors(Color.white,Color.red,Color.orange);
	   setBulletColor(Color.yellow); 
         firstScan = true;             
         
         // Ajustar o radar para a virada da arma
         setAdjustRadarForGunTurn(true);
         // Ajustar o radar para a virada do robô
         setAdjustGunForRobotTurn(true);
         
         // Loop infinito
         while(true){
            // Virar o radar para a direita infinitamente
            turnRadarRight(Double.POSITIVE_INFINITY);
         }
      	
      }
   
   /**
    * onScannedRobot: O que fazer quando você vê outro robô
    */
       public void onScannedRobot(ScannedRobotEvent e) {
         // Ângulo do robô inimigo em radianos
         double headingRadians;
         // Distância do robô inimigo
         double eDistance;
         // Ângulo do robô inimigo em radianos
         double eHeadingRadians;
         // Ângulo absoluto do robô inimigo
         double absbearing=e.getBearingRadians()+ (headingRadians = getHeadingRadians());
         // Localização do meu robô
         Point2D.Double myLocation = new Point2D.Double(getX(), getY());
         // Verificar se o robô inimigo está muito perto ou se é um "rammer", daqueles robôs que batem e causam dano
         boolean rammer = (eDistance = e.getDistance()) < 100 || getTime() < 20; 
         
         // Para o Campo de batalha
         Rectangle2D.Double field = new Rectangle2D.Double(17.9,17.9,764.1,564.1);
            
      		
         // Para os Movimentos: \/  \/
         double v1, v2, offset = Math.PI/2 + 1 - eDistance/600;
         
         // Enquanto o campo de batalha não contém o local projetado
         while(!field.contains(project(myLocation, v2 = absbearing + direction*(offset -= 0.02), 160))
         // contains(getX() + 160 * Math.sin(v2 = absbearing + direction * (offset -= .02)), getY() + 160 * Math.cos(v2))
         );
      
         // Se o robô inimigo está muito perto ou se é um "rammer" de longa distância
         if((flat && !rammer && 
         
         //O cálculo para determinar se o robô deve se aproximar com
         //base na velocidade do projétil adversário e de sua distância até o oponente
         Math.random() <  0.6*Math.sqrt(bulletVelocity/eDistance) - 0.04
         
         ) || 
          offset < Math.PI/4 ) {
            // Mudar a direção
            direction = -direction;
         }
         // Esse método é usado para ajustar a direção do robô.
         setTurnRightRadians(Math.tan(v2 -= headingRadians));
         
         // Delta de energia do robô inimigo
         double deltaE = (lastEnemyEnergy - (lastEnemyEnergy = e.getEnergy()));
         
         // Se o robô inimigo está muito perto ou se é um "rammer"
         if((0 < deltaE && deltaE < 3.001) || flat || rammer){
            // Se Move para frente
            setAhead((37 + ((int)(deltaE - 0.50001))*11) *Math.signum(Math.cos(v2)));
         }
      	
         
         
      	// movimentos, fim da função /\  /\
         
      	//Para o canhão: \/   \/
         //informações de movimento do oponente:
         //1-a variação de ângulo
         //2-a velocidade
         double w = lastEnemyHeading - (lastEnemyHeading = eHeadingRadians = e.getHeadingRadians());
         double speed = e.getVelocity();
         if(!firstScan)
            //a variável data é atualizada com as informações de movimento do oponente
            data.insert(0,(char)(w*angleScale))
               .insert(0,(char)(Math.round(speed*velocityScale)));
         
         // Tamanho da chave, ela é que determina a posição prevista do oponente. 
         int keyLength = Math.min(data.length(), Math.min((int)getTime(), 256));
         
         // Índice da chave, calculado a partir da variável data
         int index = -1;
         do{
            keyLength/=2;
            // A variável data é usada para calcular a posição prevista do oponente com base em seu histórico de movimentos
            index = data.indexOf(data.substring(0, keyLength),((int)eDistance)/11)
               /2;//ajusta números pares e até ímpares
            
         }while(index == 0 && keyLength >  1);
         
         // Potência do projétil
         double bulletPower = rammer?3:Math.min(2,Math.min(getEnergy()/16, lastEnemyEnergy/2));
         
         // Posição prevista do inimigo
         Point2D.Double predictedPosition = project(myLocation, absbearing, eDistance);
         
         // localização atual do Wrecker no Campo de Batalha
         myLocation = project(myLocation, headingRadians , getVelocity());
         
         // Ângulo do projétil
         double db=0;
         double ww=eHeadingRadians; 
         do
         {        
            if( index*(getRoundNum() + getTime()%2) > 0 ){
               speed = (((short)data.charAt(index*2))/velocityScale);
               w = (((short)data.charAt(index--*2 - 1)))/(angleScale) ;    
            }
         }while ((db+=(20-3*bulletPower))< myLocation.distance(predictedPosition = project(predictedPosition, ww-=w , speed)) 
         && field.contains(predictedPosition));         
         
         // Vira a arma para a direita
         setTurnGunRightRadians(Utils.normalRelativeAngle(Math.atan2(predictedPosition.x - myLocation.x, predictedPosition.y - myLocation.y) - getGunHeadingRadians()));
         // Atira
         setFire(bulletPower);
      	// Vira o radar para a direita
         setTurnRadarRightRadians(Math.sin(absbearing - getRadarHeadingRadians())*2);
               
         firstScan = false;
         
      	// Canhão, fim da funcão /\  /\
      
      }
      // Quando atingido por um projétil
       public void onHitByBullet(HitByBulletEvent e){
         
         lastEnemyEnergy += 20 - (bulletVelocity = e.getVelocity());	
        
         // if(hits++ > 6 && getRoundNum() < 5 )
            // flat = true;
      }
      // Quando morre
       public void onDeath(DeathEvent e){
       
         if(getRoundNum() < 3)
            flat = true;
      
      }
      // Quando acerta o inimigo
       public void onBulletHit(BulletHitEvent e){
         // double bp = e.getBullet().getPower();
         // lastEnemyEnergy -= Math.max( bp*4 +,bp*6 - 2);
         lastEnemyEnergy -= 10;
         
      }
      // Esse método pega a posição prevista do oponente com base na sua velocidade e direção.
       Point2D.Double project(Point2D.Double location, double angle, double distance){
         return new Point2D.Double(location.x + distance*Math.sin(angle), location.y + distance*Math.cos(angle));
      }
   

   
   }
