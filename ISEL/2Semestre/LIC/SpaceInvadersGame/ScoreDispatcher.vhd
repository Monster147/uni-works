library ieee;
use ieee.std_logic_1164.all;

entity ScoreDispatcher is port (
 	Dval: in std_logic;
	Din: in std_logic_vector(6 downto 0);
	MCLK: in std_logic;
	RESET: in std_logic;
	WrD: out std_logic;
	done: out std_logic;
	Dout: out std_logic_vector(6 downto 0)
);
end ScoreDispatcher;
architecture structural of ScoreDispatcher is
component  ScoreDispatcherControl is port(
	Dval: in std_logic;
	RESET: in std_logic;
	WrD: out std_logic;
	done: out std_logic;
	CLK: in std_logic
);
end component;
begin 
U0: ScoreDispatcherControl port map (Dval=> Dval, RESET=>RESET, WrD => WrD, done => done, CLK=>MCLK);
Dout <= Din;
end structural;
