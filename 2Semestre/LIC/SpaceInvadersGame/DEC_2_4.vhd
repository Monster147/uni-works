library ieee;
use ieee.std_logic_1164.all;

entity DEC_2_4 is
port(
	S: in std_logic_vector(1 downto 0);
	E: in std_logic;
	o: out std_logic_vector(3 downto 0)
	);
end DEC_2_4;

architecture structural of DEC_2_4 is
component DEC_1_2 is
port(
	a0: in std_logic;
	e: in std_logic;
	o0: out std_logic;
	o1: out std_logic
	);
end component;

signal firstDecode: std_logic_vector(1 downto 0);
begin
u1: DEC_1_2 port map(a0 => S(1), e => E, o0 => firstDecode(0), o1 => firstDecode(1));
u2: DEC_1_2 port map(a0 => S(0), e => firstDecode(0), o0 => o(0), o1 => o(1));
u3: DEC_1_2 port map(a0 => S(0), e => firstDecode(1), o0 => o(2), o1 => o(3));
end structural;