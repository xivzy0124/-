<template>
	<div class="screen-title-container">
	  <div class="screen-title">
		<span class="title-text">基于气象的农产品价格预测平台
</span>

	  </div>
	</div>
</template>

<script>
</script>

<style scoped>
	.screen-title-container {
  text-align: center;
  margin-bottom: 30px;
  padding: 15px;
  background: linear-gradient(135deg, #1e88e5 0%, #0d47a1 100%);
  color: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
	}
	.screen-title {
  font-size: 28px;
  margin-bottom: 5px;
	}

	.screen-title::before {
	  left: 0;
	  transform: translateY(-50%);
	}
	
	.screen-title::after {
	  right: 0;
	  transform: translateY(-50%) rotate(180deg);
	}
	
	.title-text {
	  font-size: 3vw; /* 根据视口宽度自适应 */
	  font-weight: bold;
	  letter-spacing: 4px;
	  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
	  position: relative;
	  display: inline-block;
	  padding: 0 60px;
	  background: linear-gradient(to right, #2db4a0, #1a7c6c);
	  -webkit-background-clip: text;
	  color: white;
	}
	
	@media (max-width: 768px) {
	  .title-text {
	    font-size: 32px;
	  }
	}
	
	.decoration-line {
	  position: absolute;
	  bottom: -10px;
	  left: 50%;
	  transform: translateX(-50%);
	  width: 80%;
	  height: 2px;
	  background: linear-gradient(
	    90deg,
	    transparent,
	    /*#3c9bd6,*/
		#2db4a0,
	    transparent
	  );
	  opacity: 0.8;
	}
</style>