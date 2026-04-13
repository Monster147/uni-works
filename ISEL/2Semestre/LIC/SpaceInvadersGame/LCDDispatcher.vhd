library ieee;
use ieee.std_logic_1164.all;

entity LCDDispatcher is port (
 	Dval: in std_logic;
	Din: in std_logic_vector(8 downto 0);
	MCLK: in std_logic;
	RESET: in std_logic;
	WrL: out std_logic;
	done: out std_logic;
	Dout: out std_logic_vector(8 downto 0)
);
end LCDDispatcher;
architecture structural of LCDDispatcher is
component  LCDDispatcherControl is port(
	Dval: in std_logic;
	Reset: in std_logic;
	WrL: out std_logic;
	done: out std_logic;
	CLK: in std_logic
);
end component;
begin 
U0: LCDDispatcherControl port map (Dval=> Dval,RESET=>RESET ,WrL => WrL, done => done, CLK=>MCLK);
Dout<= Din;
end structural;