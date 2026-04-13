library ieee;

use ieee.std_logic_1164.all;

entity Flags is
port(
	a3: in std_logic;
	b3: in std_logic;
	iCBo: in std_logic;
	Ri: in std_logic;
	OVo: out std_logic;
	CBo: out std_logic
 );
end Flags;

architecture NFL of Flags is
begin
OVo <= (a3 and b3 and (not Ri)) or ((not a3) and (not b3) and Ri);
CBo <= iCBo;
end NFL;