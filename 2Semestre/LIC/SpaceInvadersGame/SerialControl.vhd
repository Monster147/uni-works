library ieee;

use ieee.std_logic_1164.all;

entity SerialControl is port(
	CLK: in std_logic;
	init: out std_logic;
	wr: out std_logic;
	DX_VAL: out std_logic;
	RESET: in std_logic;
	accept: in std_logic;
	pFlag: in std_logic;
	dFlag: in std_logic;
	RXerror: in std_logic;
	enRX: in std_logic
	
);
end SerialControl;

architecture behavioral of SerialControl is 
type STATE_TYPE is (STATE_I, STATE_M, STATE_F, STATE_FI, STATE_FII);

signal CURRENT_STATE, NEXT_STATE : STATE_TYPE;
begin
CURRENT_STATE<= STATE_I when RESET='1' else NEXT_STATE when rising_edge(CLK);

GENERATENEXTSTATE:
process (CURRENT_STATE,enRX, RXerror,dFlag, pFlag,accept)
	begin
	case CURRENT_STATE is							 
		when STATE_I => if (enRX='0') then
							 NEXT_STATE <= STATE_M;
							 else 
							 NEXT_STATE <= STATE_I;
							 end if;
							 
		when STATE_M => if (enRX='1')then 
			                 NEXT_STATE <= STATE_I;
							 elsif (enRX='0' and dFlag='1') then 
							    NEXT_STATE <= STATE_F;
							 else
							    NEXT_STATE <= STATE_M;
							 end if;
							 
		when STATE_F => if (pFlag='1') then
							NEXT_STATE<= STATE_FII; 
							else
							NEXT_STATE<= STATE_F;
							end if;
		when STATE_FII => if ( RXerror= '1') then
							  NEXT_STATE<= STATE_I;	
							  else 
							  NEXT_STATE<= STATE_FI; 
							  end if;					
		when STATE_FI => if (accept = '1') then
							  NEXT_STATE<= STATE_I;	
							  else 
							  NEXT_STATE<= STATE_FI; 
							  end if;
							
		end case;
end process;    
init<= '1' when ((CURRENT_STATE = STATE_I))
			else '0';
wr<= '1' when ((CURRENT_STATE = STATE_M))
			else '0';
DX_VAL<= '1' when ((CURRENT_STATE = STATE_FI))
			else '0';
end behavioral;